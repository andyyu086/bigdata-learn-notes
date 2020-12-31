 ## 基本用法

 ### 安装
- 下载apache-flume-1.9.0-bin.tar，然后解压即可使用

 ### 配置
```shell
[apache-flume-1.9.0-bin]# cat ./conf/eml.conf 
#3个组件命名配置
eml.sources = exe_src
eml.sinks = log_sink
eml.channels = mem_chan

#source读取服务器日志地址
eml.sources.exe_src.type = exec
eml.sources.exe_src.command = tail -F /opt/apache-flume-1.9.0-bin/test_data/access.log

#channel为内存，sink为直接打印日志
eml.channels.mem_chan.type = memory

eml.sinks.log_sink.type = logger

#source和sink配置到对应的channel
eml.sources.exe_src.channels = mem_chan
eml.sinks.log_sink.channel = mem_chan
```

 ### 使用
```shell
./bin/flume-ng agent --name eml --conf ./conf --conf-file ./conf/eml.conf -Dflume.root.logger=INFO,console
```

### 配合kafka作为sink的场景
```
emk.sources = exe_src
emk.sinks = kaf_sink
emk.channels = mem_chan

emk.sources.exe_src.type = exec
emk.sources.exe_src.command = tail -F /opt/apache-flume-1.9.0-bin/test_data/access.log

emk.channels.mem_chan.type = memory

emk.sinks.kaf_sink.type = org.apache.flume.sink.kafka.KafkaSink
emk.sinks.kaf_sink.brokerList = datateam-master:9092
emk.sinks.kaf_sink.topic = my-flume-topic
emk.sinks.kaf_sink.batchSize = 5
emk.sinks.kaf_sink.requiredAck = 1

emk.sources.exe_src.channels = mem_chan
emk.sinks.kaf_sink.channel = mem_chan
```
- 保存为emk.conf;
- 然后执行
```
./bin/flume-ng agent --name emk --conf ./conf --conf-file ./conf/emk.conf -Dflume.root.logger=INFO,console
```
