

## flink对kafka offset提交的策略
The way to configure offset commit behaviour is different, depending on whether or not checkpointing is enabled for the job.

Checkpointing disabled: 
if checkpointing is disabled, the Flink Kafka Consumer relies on the automatic periodic offset committing capability of the internally used Kafka clients. Therefore, to disable or enable offset committing, simply set the enable.auto.commit (or auto.commit.enable for Kafka 0.8) / auto.commit.interval.ms keys to appropriate values in the provided Properties configuration.

Checkpointing enabled: 
if checkpointing is enabled, the Flink Kafka Consumer will commit the offsets stored in the checkpointed states when the checkpoints are completed. This ensures that the committed offsets in Kafka brokers is consistent with the offsets in the checkpointed states. Users can choose to disable or enable offset committing by calling the setCommitOffsetsOnCheckpoints(boolean) method on the consumer (by default, the behaviour is true). Note that in this scenario, the automatic periodic offset committing settings in Properties is completely ignored.

## 使用布隆过滤器实现UV实时计算
- BloomFilter判断一个元素不在集合中的时候的错误率。 BloomFilter判断该元素不在集合中，则该元素一定不再集合中。故False negatives概率为0.

- 算法描述： BloomFilter使用长度为m bit的字节数组，使用k个hash函数，
增加一个元素: 通过k次hash将元素映射到字节数组中k个位置中，并设置对应位置的字节为1.
查询元素是否存在: 将元素k次hash得到k个位置，如果对应k个位置的bit是1则认为存在，反之则认为不存在。

```scala
class UvCountWithBloom() extends ProcessWindowFunction[(String,Long),UvCount,String,TimeWindow]{
  // 定义redis连接
  lazy val jedis = new Jedis("localhost", 6379)
  //设置位图的大小，最大2^32 即512MB
  lazy val bloom = new Bloom(1<<29)

  override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[UvCount]): Unit = {
    //window的end，设置位redis的key
    val storeKey = context.window.getEnd.toString
    var count = 0L
    if (jedis.hget("count",storeKey)!=null){
      count = jedis.hget("count",storeKey).toLong
    }

    val userI = elements.last._2.toString
    //将用户名换算为offset
    val offset = bloom.hash(userI,61)

    //判断该用户位是否存在
    val isExist = jedis.getbit(storeKey,offset)

    //不存在，FN的概率为0
    if (!isExist){
        //设置位和计数count
      jedis.setbit(storeKey,offset,true)
      jedis.hset("count",storeKey,(count+1).toString)
    }else{
      out.collect(UvCount(storeKey.toLong,count))
    }
  }
}

class Bloom(size: Long) extends Serializable{
  // 位图的总大小，默认16M
  private val cap = if (size > 0) size else 1 << 27

  // 定义hash函数
  def hash(value: String, seed: Int): Long = {
    var result = 0L
    for( i <- 0 until value.length ){
      result = result * seed + value.charAt(i)
    }
    result  & ( cap - 1 )
  }

}
```

## CEP的实现例子
Flink为CEP提供了专门的Flink CEP library，它包含如下组件：

- Event Stream
- pattern定义
每个Pattern都应该包含几个步骤，或者叫做state。从一个state到另一个state，通常我们需要定义一些条件，例如下列的代码：
```scala
val loginFailPattern = Pattern.begin[LoginEvent]("begin")
  .where(_.eventType.equals("fail"))
  .next("next")
  .where(_.eventType.equals("fail"))
  .within(Time.seconds(10)
```
每个state都应该有一个标示：
例如: .begin[LoginEvent]("begin")中的"begin"
每个state都需要有一个唯一的名字，而且需要一个filter(where)来过滤条件，这个过滤条件定义事件需要符合的条件
例如: .where(_.eventType.equals("fail"))

通过next或者followedBy方法切换到下一个state，next的意思是说上一步符合条件的元素之后紧挨着的元素；
而followedBy并不要求一定是挨着的元素。这两者分别称为严格近邻和非严格近邻。
val strictNext = start.next("middle")
val nonStrictNext = start.followedBy("middle")

最后，我们可以将所有的Pattern的条件限定在一定的时间范围内：
next.within(Time.seconds(10))

- pattern检测
通过一个input DataStream以及刚刚我们定义的Pattern，可以创建一个PatternStream;
获得PatternStream，我们就可以通过select或flatSelect，从一个Map序列找到我们需要的告警信息;

- 超时事件的处理
通过within方法，我们的parttern规则限定在一定的窗口范围内。
当有超过窗口时间后还到达的event，我们可以通过在select或flatSelect中，实现PatternTimeoutFunction/PatternFlatTimeoutFunction来处理.

具体的一个例子:

```scala
// 定义输入订单事件的样例类
case class OrderEvent(orderId: Long, eventType: String, txId: String, eventTime: Long)

// 定义输出结果样例类
case class OrderResult(orderId: Long, resultMsg: String)


object OrderTimeout {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    // 1. 读取订单数据
    val resource = getClass.getResource("/OrderLog.csv")
    //    val orderEventStream = env.readTextFile(resource.getPath)
    val orderEventStream = env.socketTextStream("localhost", 8888)
      .map(data => {
        val dataArray = data.split(",")
        OrderEvent(dataArray(0).trim.toLong, dataArray(1).trim, dataArray(2).trim, dataArray(3).trim.toLong)
      })
      .assignAscendingTimestamps(_.eventTime * 1000L)
      .keyBy(_.orderId)

    val orderPayPattern = Pattern.begin[OrderEvent]("begin").where(_.eventType == "create")
      .followedBy("follow").where(_.eventType == "pay")
      .within(Time.minutes(15))

    val patternStream = CEP.pattern(orderEventStream,orderPayPattern)

    val orderTimeoutOutputTag = new OutputTag[OrderResult]("orderTimeout")

    val resultStream = patternStream.select(orderTimeoutOutputTag,
      new OrderTimeoutSelect(),
      new OrderPaySelect())

    resultStream.print("payed")
    resultStream.getSideOutput(orderTimeoutOutputTag).print("timeout")

    env.execute("order timeout job")
  }
}
// 自定义超时事件序列处理函数
class OrderTimeoutSelect() extends PatternTimeoutFunction[OrderEvent,OrderResult]{
  override def timeout(map: util.Map[String, util.List[OrderEvent]], l: Long): OrderResult = {
    val timeoutOrderId = map.get("begin").iterator().next().orderId
    OrderResult(timeoutOrderId,"timeout")

  }
}
// 自定义正常支付事件序列处理函数
class OrderPaySelect() extends PatternSelectFunction[OrderEvent,OrderResult]{
  override def select(map: util.Map[String, util.List[OrderEvent]]): OrderResult = {
    val payedOrderId = map.get("follow").iterator().next().orderId
    OrderResult(payedOrderId, "payed successfully")
  }
}
```

## Stream Join的代码例子
```scala
    val processStream = orderEventStream.intervalJoin(receiptEventStream)
      .between(Time.seconds(-5),Time.seconds(5))
      .process(new TxPayMatchByJoin())

    processStream.print()

    env.execute("tx pay match by join job")
```
```scala
class TxPayMatchByJoin() extends ProcessJoinFunction[OrderEvent, ReceiptEvent, (OrderEvent, ReceiptEvent)]{
  override def processElement(in1: OrderEvent, in2: ReceiptEvent, context: ProcessJoinFunction[OrderEvent, ReceiptEvent, (OrderEvent, ReceiptEvent)]#Context, collector: Collector[(OrderEvent, ReceiptEvent)]): Unit = {
    collector.collect((in1, in2))
  }
}
```



