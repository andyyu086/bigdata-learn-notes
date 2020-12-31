
## Streaming

1. 简单的获取kafka消息的代码
```python
    val sc = new SparkContext(conf)

    // Duration对象中封装了时间的一个对象，它的单位是ms.
    val batchDuration = new Duration(5000)
    // batchDuration为时间间隔

    val ssc = new StreamingContext(sc, batchDuration)
    ssc.checkpoint("F:/movieck2")

    val topics = Set("test1")
    val kafkaParams = Map("metadata.broker.list" -> "movie1:9092,movie2:9092,movie3:9092")

    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
```

## 连接kafka官网文档链接
http://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html

## Checkpoint的基本原理和用途
https://zhuanlan.zhihu.com/p/54327668
- 注意和RDD persist的区别

## 读取kafka streaming的scala版本的例子
```scala
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WebStat {
  def main(args: Array[String]): Unit = {
    //配置kafka brokers清单，topic清单，grp清单
    val brokers = "192.168.3.58:9092,192.168.3.57:9092,192.168.3.56:9092"
    val topics = Array("my-flume-topic")
    val grp = "my-flume-grp"

    //获取出ssc和配置checkpoint
    val checkPointDir = "/Users/andyyu/Downloads/ckp"
    val sparkSession = SparkSession.builder().appName("wc").master("local[2]").getOrCreate()
    val sparkContext = sparkSession.sparkContext
    val ssc = new StreamingContext(sparkContext, Seconds(5))
    ssc.checkpoint(checkPointDir)

    //构建kafka参数
    val kafkaParas = Map[String,Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> grp,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false:java.lang.Boolean),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )

    //获取dstream; kafka broker跟spark匹配; 消费参数
    val dStream : InputDStream[ConsumerRecord[String,String]] = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](topics,kafkaParas))

    //map reduceByKey算法; 实现词频
    val dSmap = dStream.map(record => record.value())
    val res: DStream[(String, Int)] = dSmap.map((_,1)).reduceByKey(_+_)
    res.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
```

