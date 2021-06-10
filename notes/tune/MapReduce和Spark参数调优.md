## 1.mapreduce参数
### 1.1 map内存参数调整举例
```
mapreduce.map.memory.mb=2240   # Container size
mapreduce.map.java.opts=-Xmx2016m  # JVM arguments for a Map task
```
> [EMR官方配置文档链接](https://docs.aws.amazon.com/zh_cn/emr/latest/ReleaseGuide/emr-hadoop-task-config.html#emr-hadoop-task-config-m1)

- 例如,m1.xlarge MapReduce 任务性能调整配置表

配置选项 | 默认值 | 参数解释
---|---|---
mapreduce.map.java.opts | -Xmx512m | map JVM最大堆内存
mapreduce.reduce.java.opts | -Xmx1536m | reduce JVM最大堆内存
mapreduce.map.memory.mb | 768 | map分配的内存量，最后会换算成++最**小**增量++的倍数
mapreduce.reduce.memory.mb | 2048 | 类似上方map参数
yarn.app.mapreduce.am.resource.mb | 2048 | AM被分配内存
yarn.scheduler.minimum-allocation-mb | 256 | yarn分配给container的内存最**小**增量
yarn.scheduler.maximum-allocation-mb | 8192 | yarn分配给container的内存最**大**增量
yarn.nodemanager.resource.memory-mb | 12288 | NM被分配的总内存

- m1.xlarge hadoop本身的参数配置表

参数 | 值
---|---
YARN_RESOURCEMANAGER_HEAPSIZE | 1024
YARN_PROXYSERVER_HEAPSIZE | 512
YARN_NODEMANAGER_HEAPSIZE | 768
HADOOP_JOB_HISTORYSERVER_HEAPSIZE | 1024
HADOOP_NAMENODE_HEAPSIZE | 2304
HADOOP_DATANODE_HEAPSIZE | 384

### 1.2 yarn参数调整
> [详细解释，可参考MapR的分析文档](https://mapr.com/blog/best-practices-yarn-resource-management/)
#### 1.2.1 总量参数
- CPU 内存 IO三个部分,yarn可分配的参数分别为:
1. yarn.nodemanager.resource.memory-mb
2. yarn.nodemanager.resource.cpu-vcores
3. yarn.nodemanager.resource.io-spindles

- 可能出现yarn分配内存不足的报错信息：
> os::commit_memory(0x0000000000000000, xxxxxxxxx, 0) failed;
error=’Cannot allocate memory’ (errno=12)

#### 1.2.2 增量参数
比如上表所列举的：
yarn.scheduler.minimum-allocation-mb

#### 1.2.3 virtual memory和physical memory超限
当参数yarn.nodemanager.pmem-check-enabled设为true的情况下，如果使用物理内存超过mapreduce.map.memory.mb，就会触发物理内存超限异常。
- 物理内存超限的报错信息
> Current usage: <font color="red">2.1gb of 2.0gb physical memory used</font>;
1.1gb of 3.15gb virtual memory used. Killing container.
- 虚拟内存超限的报错信息：
> Current usage: 347.3 MB of 1 GB physical memory used;
<font color="red">2.2 GB of 2.1 GB virtual memory used</font>. Killing container.


## 2. Spark参数
### 2.1 excutor内存相关参数举例
```
--executor-memory 6g 
--conf spark.yarn.executor.memoryOverhead=2048
```
### 2.2 集群内基本参数列表
> [参考c2fo.io的文档](https://c2fo.io/c2fo/spark/aws/emr/2016/07/06/apache-spark-config-cheatsheet/)

参数 | 默认 | 描述
---|---|---
spark.executor.memory | x | 每个executor分配的内存量
spark.executor.cores | x | 每个executor使用的cpu虚拟核数
spark.executor.instances | | 节点数 * 每节点的 Executor数 - 1
spark.yarn.executor.memoryOverhead | | 每Executor堆外内存
spark.executor.memory | | 每Executor内存
spark.yarn.driver.memoryOverhead | | 类似于 spark.yarn.executor.memoryOverhead
spark.driver.memory | | 类似于spark.executor.memory
spark.driver.cores | | 类似于spark.executor.cores
spark.default.parallelism | | 等于spark.executor.instances * spark.executor.cores * Parallelism Per Core
spark.dynamicAllocation.enabled | | 是否开启动态资源分配
maximizeResourceAllocation || 基于集群的可用计算和内存资源最大化设置分配

- 启用maximizeResourceAllocation会自动配置的设置有：

设置 | 描述 | 值
--- | --- | ---
spark.default.parallelism | 在用户未设置的情况下由Transform算子 (如联接、reduceByKey 和并行化) 返回的RDD中的分区数 | 对YARN容器可用的CPU内核数的2倍
spark.driver.memory | 要用于Driver进程（即初始化 SparkContext）的内存量 | 基于集群中的实例类型配置设置。但是，由于 Spark 驱动程序可在主实例或某个核心实例 (例如，分别在 YARN 客户端和集群模式中) 上运行，因此将根据这两个实例组中的实例类型的较小者进行设置
spark.executor.memory | 每个执行者进程要使用的内存量 | 基于集群中的核心和任务实例类型配置设置
spark.executor.cores | 要对每个执行程序使用的内核的数量 | 基于集群中的核心和任务实例类型配置设置
spark.executor.instances | 执行程序数 | 基于集群中的核心和任务实例类型配置设置。除非同时将 spark.dynamicAllocation.enabled 显式设置为 true，否则将设置。


### 2.3 Spark SQL优化性能参数点
参数 | 解释
--- | ---
spark.sql.dynamicPartitionPruning.enabled | 
spark.sql.optimizer.flattenScalarSubqueriesWithAggregates.enabled | 
spark.sql.optimizer.distinctBeforeIntersect.enabled | 
spark.sql.bloomFilterJoin.enabled | 
spark.sql.optimizer.sizeBasedJoinReorder.enabled | 

### 2.4 Spark 内存配置
![image](https://d2908q01vomqb2.cloudfront.net/b6692ea5df920cad691c20319a6fffd7a4a766b8/2019/04/02/SparAppsonEMR1.png)

### 2.5 Spark executor相关调优
https://www.jianshu.com/p/3716ade93b02

