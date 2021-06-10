
## 消费者设置
- 同一个分区只能被同一个消费者群组里面的一个消费者读取，不可能存在同一个分区被同一个消费者群里多个消费者共同读取的情况
- 如果消费者比较多，即便消费者有空闲了，但是也不会去读取任何一个已被其他消费者消费的分区的数据，这同时也提醒我们在使用时应该合理设置消费者的数量，以免造成闲置和额外开销。

## 消费分区再平衡
- 在新增分区或者消费者退出的场景下，通过轮询或者offset提交时发送的心跳超时触发下，群组协调器会判断消费者的存活；从而进行对应分区的消费者的调整
- 整体保证了消费组的可靠性和自动扩容

## 偏移量
- 默认消费者消费时会在kafka的＿consumer_offset这一特殊topic记录；正常情况下，该偏移量没有什么用途；
- 但是在分区再平衡的时候，新的消费者需要知道之前的消费者消费的位置，从而继续开始消费；
- 如果实际最后消费的位置和记录的偏移量不一致的话，就会导致重复消费或者漏消费的问题。

### offset管理
手动提交offset
https://www.jianshu.com/p/d2a61be73513

## 消费者设置
https://github.com/heibaiying/BigData-Notes/blob/master/notes/Kafka消费者详解.md

### offset手动提交
```scala
    //2.使用KafkaUtil连接Kafak获取数据
    val recordDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](ssc,
      LocationStrategies.PreferConsistent,//位置策略,源码强烈推荐使用该策略,会让Spark的Executor和Kafka的Broker均匀对应
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams))//消费策略,源码强烈推荐使用该策略

    var offsetRanges: Array[OffsetRange] = Array.empty[OffsetRange]
    recordDStream.transform(rdd => {
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }).map(record => {(record.value,1)}
    ).foreachRDD(rdd => {recordDStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)})
```

- 通过recordDStream的transform操作获取offsetRanges，计算完成后，通过foreachRDD，CanCommitOffsets的commitAsync进行offset的异步提交；
- 此外，可以通过一下命令进行,__consumer_offsets的offset提交偏移查看；
```shell
bin/kafka-console-consumer.sh --topic __consumer_offsets --partition 46 --bootstrap-server 192.168.3.58:9092,192.168.3.57:9092,192.168.3.56:9092 --formatter "kafka.coordinator.group.GroupMetadataManager\$OffsetsMessageFormatter" --consumer.config config/consumer.properties.new --timeout-ms 180000
```
- 其中，需要在配置文件config/consumer.properties.new中加入exclude.internal.topics=false的配置；
- --partition的计算方法为: (结果为46)
```java
        int numPartitions = 50;
        String groupID = "my-flume-grp";
        int i = Math.abs(groupID.hashCode()) % numPartitions;
        System.out.println(i);
```
- 执行后的结果为:
> [my-flume-grp,my-flume-topic,0]::OffsetAndMetadata(offset=8397536, leaderEpoch=Optional.empty, metadata=, commitTimestamp=1609992973699, expireTimestamp=None)
[my-flume-grp,my-flume-topic,0]::OffsetAndMetadata(offset=8397536, leaderEpoch=Optional.empty, metadata=, commitTimestamp=1609992978701, expireTimestamp=None)
可以看到__consumer_offsets topic的每一日志项的格式都是：[Group, Topic, Partition]::[OffsetMetadata[Offset, Metadata], CommitTime, ExpirationTime]
- 如果手动提交offset的话，可以看到消费后offset的变化；如果没有提交的话，该命令行会达到timeout 3分钟后，自动退出。

## 生产者的配置
- 异步发送
通常我们并不关心发送成功的情况，更多关注的是失败的情况，因此 Kafka 提供了异步发送和回调函数。 代码如下：
```scala
for (int i = 0; i < 10; i++) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "k" + i, "world" + i);
    /*异步发送消息，并监听回调*/
    producer.send(record, new Callback() {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                System.out.println("进行异常处理");
            } else {
                System.out.printf("topic=%s, partition=%d, offset=%s \n",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        }
    });
}
```

- 分区器
1. 默认分区器
Kafka 有着默认的分区机制：
如果键值为 null， 则使用轮询 (Round Robin) 算法将消息均衡地分布到各个分区上；
如果键值不为 null，那么 Kafka 会使用内置的散列算法对键进行散列，然后分布到各个分区上。
某些情况下，你可能有着自己的分区需求，这时候可以采用自定义分区器实现。这里给出一个自定义分区器的示例：

2. 自定义分区器
```scala
/**
 * 自定义分区器
 */
public class CustomPartitioner implements Partitioner {

    private int passLine;

    @Override
    public void configure(Map<String, ?> configs) {
        /*从生产者配置中获取分数线*/
        passLine = (Integer) configs.get("pass.line");
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, 
                         byte[] valueBytes, Cluster cluster) {
        /*key 值为分数，当分数大于分数线时候，分配到 1 分区，否则分配到 0 分区*/
        return (Integer) key >= passLine ? 1 : 0;
    }

    @Override
    public void close() {
        System.out.println("分区器关闭");
    }
}
```
需要在创建生产者时指定分区器，和分区器所需要的配置参数：
```scala
public class ProducerWithPartitioner {

    public static void main(String[] args) {

        String topicName = "Kafka-Partitioner-Test";

        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        /*传递自定义分区器*/
        props.put("partitioner.class", "com.heibaiying.producers.partitioners.CustomPartitioner");
        /*传递分区器所需的参数*/
        props.put("pass.line", 6);

        Producer<Integer, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i <= 10; i++) {
            String score = "score:" + i;
            ProducerRecord<Integer, String> record = new ProducerRecord<>(topicName, i, score);
            /*异步发送消息*/
            producer.send(record, (metadata, exception) ->
                    System.out.printf("%s, partition=%d, \n", score, metadata.partition()));
        }

        producer.close();
    }
}
```
- 其他参数
1. acks

acks 参数指定了必须要有多少个分区副本收到消息，生产者才会认为消息写入是成功的：

acks=0 ： 消息发送出去就认为已经成功了，不会等待任何来自服务器的响应；
acks=1 ： 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应；
acks=all ：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。

## 副本
- sendfile和transferTo实现零拷贝

Linux 2.4+ 内核通过 sendfile 系统调用，提供了零拷贝。数据通过 DMA 拷贝到内核态 Buffer 后，直接通过 DMA 拷贝到 NIC Buffer，无需 CPU 拷贝。这也是零拷贝这一说法的来源。除了减少数据拷贝外，因为整个读文件到网络发送由一个 sendfile 调用完成，整个过程只有两次上下文切换，因此大大提高了性能。

## KE 客户端安装
https://www.jianshu.com/p/db9f37bb7f98

## Kakfa Eagle
- 需要配置开通JMS Port才可以访问
- 使用 KSQL 注意topic名包含横线时解析有问题：成功的语句例子如下：
> select * from topic_flink_szt_new where `partition` in (0) limit 10

## 启动
- nohup sh ke.sh start &


