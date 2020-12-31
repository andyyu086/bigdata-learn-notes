## 简单介绍

### 安装
下载**kafka_2.11-2.1.1.tgz**安装包，解压即可。

### 配置
在conf目录内，进行server.properties配置文件的配置
```properties
#配置broker id，用于节点的唯一标识
broker.id=1
#配置监听地址和端口
listeners=PLAINTEXT://datateam-master:9092
advertised.listeners=PLAINTEXT://datateam-master:9092

num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

#配置kafka时间消息数据的保存地址
log.dirs=/data/opt/kafka-logs
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000

#配置zookeeper的监听信息
zookeeper.connect=192.168.3.58:2181,192.168.3.57:2181,192.168.3.56:2181
zookeeper.connection.timeout.ms=6000
group.initial.rebalance.delay.ms=0
```
- 注意以上broker.id参数，每个节点要配置的不一样。

### 启动
- 在每个节点启动一下命令即可
```shell
sh /data/opt/kafka_2.11-2.1.1/bin/kafka-server-start.sh -daemon /data/opt/kafka_2.11-2.1.1/config/server.properties 
```

### Topic操作
- 查看所有topic
```
[bin]# ./kafka-topics.sh --zookeeper 192.168.3.58:2181 --list
```

- 新建topic
```
[bin]# ./kafka-topics.sh --create  --zookeeper 192.168.3.58:2181 --replication-factor 3 --partitions 1 --topic my-flume-topic
```

- 查看topic
```
[bin]# ./kafka-topics.sh --describe  --zookeeper 192.168.3.58:2181 --topic my-flume-topic
```

- 消费topic
```
[bin]# ./kafka-console-consumer.sh --bootstrap-server datateam-master:9092 --topic my-flume-topic --from-beginning
```

- 打开JMX 端口
在start脚本启动之前将JMX端口加入环境变量即可；此外，可以iptables确认一下防火墙情况；
最后使用`tcping [ip] 9988`命令，在本机检查一下服务器端口是否open.
export JMX_PORT=9988





