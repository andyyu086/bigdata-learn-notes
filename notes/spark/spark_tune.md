
## OOM
- 对于”Consider boosting spark.yarn.executor.memoryOverhead“的报错，需要增加堆外内存；

arnSchedulerBackend$YarnSchedulerEndpoint: Requesting driver to remove executor 21 for reason Container killed by YARN for exceeding memory limits. 4.1 GB of 4 GB physical memory used. Consider boosting spark.yarn.executor.memoryOverhead or disabling yarn.nodemanager.vmem-check-enabled
409 512

此外，增加堆外内存的同时需要注意executor的总内存超过yarn node manager的内存的时候，需要同步调整yarn的参数；
一个m1 large机型的操作例子:
> nohup spark-submit \
   --executor-memory 3G \
   --conf spark.yarn.executor.memoryOverhead=2048 \
   --conf yarn.scheduler.maximum-allocation-mb=6400 \
   --conf yarn.nodemanager.resource.memory-mb=6528 \
   --conf yarn.nodemanager.vmem-check-enabled=false \

> yarn相关内存参数解释:
yarn.nodemanager.resource.memory-mb //每个NodeManager可以供yarn调度（分配给container）的物理内存，单位MB
yarn.nodemanager.resource.cpu-vcores  //每个NodeManager可以供yarn调度（分配给container）的vcore个数
 
yarn.scheduler.maximum-allocation-mb //每个container能够申请到的最大内存
yarn.scheduler.minimum-allocation-mb //每个container能够申请到的最小内存，如果设置的值比该值小，默认就是该值
yarn.scheduler.increment-allocation-mb //container内存不够用时一次性加多少内存 单位MB。CDH默认512M
yarn.scheduler.minimum-allocation-vcores //每个container能够申请到的最小vcore个数，如果设置的值比该值小，默认就是该值
yarn.scheduler.maximum-allocation-vcores //每个container能够申请到的最大vcore个数。
 
yarn.nodemanager.pmem-check-enabled //是否对contanier实施物理内存限制，会通过一个线程去监控container内存使用情况，超过了container的内存限制以后，就会被kill掉。
yarn.nodemanager.vmem-check-enabled //是否对container实施虚拟内存限制

> spark相关内存参数解释：
executor-memory executor分配的堆内存
spark.yarn.executor.memoryOverhead 每个executor分配的堆外内存
默认堆外内存的计算方法：
> 源代码如下:
val MEMORY_OVERHEAD_FACTOR = 0.10
val MEMORY_OVERHEAD_MIN = 384L

// Executor memory in MB.
protected val executorMemory = sparkConf.get(EXECUTOR_MEMORY).toInt
// Additional memory overhead.
protected val memoryOverhead: Int = sparkConf.get(EXECUTOR_MEMORY_OVERHEAD).getOrElse(
  math.max((MEMORY_OVERHEAD_FACTOR * executorMemory).toInt, MEMORY_OVERHEAD_MIN)).toInt
- 解释一下就是最小384MB，然后跟executorMemory*0.1的值，取max就行；当然，分配的最小单位参考yarn.scheduler.increment-allocation-mb的增量来操作；
- 在调整overhead内存无法解决的情况下，可以进一步降低executor cores的数量，从而降低并行度，降低NIO的堆外内存的使用情况；当然会牺牲整个程序的性能。

>可参考博客:
https://www.cnblogs.com/zz-ksw/p/11403622.html
https://medium.com/analytics-vidhya/solving-container-killed-by-yarn-for-exceeding-memory-limits-exception-in-apache-spark-b3349685df16


## Executor个数设置
- 官网基本计算方式:
YARN: The --num-executors option to the Spark YARN client controls how many executors it will allocate on the cluster (spark.executor.instances as configuration property), 
while --executor-memory (spark.executor.memory configuration property) and --executor-cores (spark.executor.cores configuration property) control the resources per executor. 
其中spark.executor.instances 在yarn下，官网默认为2；

## Resource Allocation Policy
At a high level, Spark should relinquish executors when they are no longer used and acquire executors when they are needed. Since there is no definitive way to predict whether an executor that is about to be removed will run a task in the near future, or whether a new executor that is about to be added will actually be idle, we need a set of heuristics to determine when to remove and request executors.
- 启动算法:定期查询待运行pending task队列，指数级上升创建executor.
Spark requests executors in rounds. The actual request is triggered when there have been pending tasks for spark.dynamicAllocation.schedulerBacklogTimeout seconds, and then triggered again every spark.dynamicAllocation.sustainedSchedulerBacklogTimeout seconds thereafter if the queue of pending tasks persists. Additionally, the number of executors requested in each round increases exponentially from the previous round. For instance, an application will add 1 executor in the first round, and then 2, 4, 8 and so on executors in the subsequent rounds.
- 关闭算法:在没有待运行task的情况下，executor idle一定的超时时间后，停止。
- shuffle的文件的保存:
为了保证executor退出后，后续excutor需要的shuffle中间文件还得以保存，新版本追加了一个常驻数据进行管理:
The solution for preserving shuffle files is to use an external shuffle service, also introduced in Spark 1.2. This service refers to a long-running process that runs on each node of your cluster independently of your Spark applications and their executors. If the service is enabled, Spark executors will fetch shuffle files from the service instead of from each other. This means any shuffle state written by an executor may continue to be served beyond the executor’s lifetime.

## Exit code 137
- AWS 参考解决办法: https://aws.amazon.com/cn/premiumsupport/knowledge-center/container-killed-on-request-137-emr/
使用以下一种或多种方法来解决“退出状态: 137”阶段故障：

- 增加驱动程序或执行程序内存
通过调整 spark.executor.memory 或 spark.driver.memory 参数来增加容器内存（取决于导致错误的容器）。
使用 --executor-memory 或 --driver-memory 选项来增加运行 spark-submit 时的内存。示例：
spark-submit --executor-memory 10g --driver-memory 10g .

- 添加更多 Spark 分区
如果您不能增加容器内存（例如，如果在节点上使用的是 maximizeResourceAllocation），则增加 Spark 分区的数量。这减少了单个 Spark 任务处理的数据量，从而减少了单个执行程序使用的总内存。使用以下 Scala 代码添加更多 Spark 分区：
val numPartitions = 500
val newDF = df.repartition(numPartitions)

- 增加shuffle分区的数量
如果在宽转换过程中发生错误（例如 join 或 groupBy），则添加更多的随机分区。默认值为 200。
运行 spark-submit 时，使用 --conf spark.sql.shuffle.partitions 选项添加更多的随机分区。示例：
spark-submit --conf spark.sql.shuffle.partitions=500 .

- 减少执行程序内核的数量
这减少了执行程序同时处理的最大任务数，从而减少了容器使用的内存量。
使用 --executor-cores 选项减少在运行 spark-submit 时执行程序内核的数量。示例：
spark-submit --executor-cores 1 

## repartition 用法:
- The simplest solution is to add one or more columns to repartition and explicitly set the number of partitions.

val numPartitions = ???

df.repartition(numPartitions, $"some_col", $"some_other_col")
 .write.partitionBy("some_col")
 .parquet("partitioned_lake")
where:

numPartitions - should be an upper bound (actual number can be lower) of the desired number of files written to a partition directory.
$"some_other_col" (and optional additional columns) should have high cardinality and be independent of the $"some_column (there should be functional dependency between these two, and shouldn't be highly correlated).

If data doesn't contain such column you can use o.a.s.sql.functions.rand.

import org.apache.spark.sql.functions.rand

df.repartition(numPartitions, $"some_col", rand)
  .write.partitionBy("some_col")
  .parquet("partitioned_lake")

- partitionBy方法表示使用some_col新建子目录，进行结果文件的保存.
- 使用repartition配合rand，可以使得按列partitionBy分区操作时，对于数据非常倾斜的情况下，再随机分拆多个文件出来；避免出现巨大文件。

- 一个数据极端倾斜的案例:
df
  .repartition(col("person_country"))
  .write
  .option("maxRecordsPerFile", 10)
  .partitionBy("person_country")
  .csv(outputPath)
- 不指定partitionBy内的具体numPartitions数量，使用maxRecordsPerFile限定每个partition的最多行即可。
- 详细可以参考这个链接:
https://mungingdata.com/apache-spark/partitionby/

## OOM GC定位和调整
1. Run spark add conf bellow:

--conf 'spark.driver.extraJavaOptions=-XX:+UseCompressedOops -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps' \
--conf 'spark.executor.extraJavaOptions=-XX:+UseCompressedOops -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC  ' \
When jvm GC ,you will get follow message:
Heap after GC invocations=157 (full 98):
 PSYoungGen      total 940544K, used 853456K [0x0000000781800000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 860160K, 99% used [0x0000000781800000,0x00000007b5974118,0x00000007b6000000)
  from space 80384K, 0% used [0x00000007b6000000,0x00000007b6000000,0x00000007bae80000)
  to   space 77824K, 0% used [0x00000007bb400000,0x00000007bb400000,0x00000007c0000000)
 ParOldGen       total 2048000K, used 2047964K [0x0000000704800000, 0x0000000781800000, 0x0000000781800000)
  object space 2048000K, 99% used [0x0000000704800000,0x00000007817f7148,0x0000000781800000)
 Metaspace       used 43044K, capacity 43310K, committed 44288K, reserved 1087488K
  class space    used 6618K, capacity 6701K, committed 6912K, reserved 1048576K  
}
Both PSYoungGen and ParOldGen are 99% ,then you will get java.lang.OutOfMemoryError: GC overhead limit exceeded if more object was created .

2. Try to add more memory for your executor or your driver when more memory resources are avaliable:
--executor-memory 10000m \
--driver-memory 10000m \

For my case : memory for PSYoungGen are smaller then ParOldGen which causes many young object enter into ParOldGen memory area and finaly ParOldGen are not avaliable.So java.lang.OutOfMemoryError: Java heap space error appear.

3. Adding conf for executor:
'spark.executor.extraJavaOptions=-XX:NewRatio=1 -XX:+UseCompressedOops -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps '
-XX:NewRatio=rate rate = ParOldGen/PSYoungGen

4. It depend ends.You can try GC strategy like
-XX:+UseSerialGC :Serial Collector 
-XX:+UseParallelGC :Parallel Collector
-XX:+UseParallelOldGC :Parallel Old collector 
-XX:+UseConcMarkSweepGC :Concurrent Mark Sweep 
Java Concurrent and Parallel GC
> the answer is you only need to use -XX:+UseConcMarkSweepGC and it will enable the concurrent collector with the parallel young generation collector.

Edit: 
for Java 6, the same flag (-XX:+UseConcMarkSweepGC) enables the concurrent collector. The choice of collector you want depends on a few things, and you should test different configurations. But there are some very general guidelines.
 If you have a single processor, single thread machine then you should use the serial collector (default for some configurations, can be enabled explicitly for with -XX:+UseSerialGC). 
 For multiprocessor machines where your workload is basically CPU bound, use the parallel collector. This is enabled by default if you use the -server flag, or you can enable it explicitly with -XX:+UseParallelGC. 
 If you'd rather keep the GC pauses shorter at the expense of using more total CPU time for GC, and you have more than one CPU, you can use the concurrent collector (-XX:+UseConcMarkSweepGC). 
 Note that the concurrent collector tends to require more RAM allocated to the JVM than the serial or parallel collectors for a given workload because some memory fragmentation can occur.
> https://www.oracle.com/java/technologies/javase/gc-tuning-6.html
https://blogs.oracle.com/jonthecollector/our-collectors


## 聚合SQL语句调优参考
1. Picking the Right Operators
- The primary goal when choosing an arrangement of operators is to reduce the number of shuffles and the amount of data shuffled. This is because shuffles are fairly expensive operations; all shuffle data must be written to disk and then transferred over the network. repartition , join, cogroup, and any of the *By or *ByKey transformations can result in shuffles.
Not all these operations are equal, however, and a few of the most common performance pitfalls for novice Spark developers arise from picking the wrong one:

- Avoid groupByKey when performing an associative reductive operation. 
For example, rdd.groupByKey().mapValues(_.sum) will produce the same results as rdd.reduceByKey(_ + _). However, the former will transfer the entire dataset across the network, while the latter will compute local sums for each key in each partition and combine those local sums into larger sums after shuffling.
- Avoid reduceByKey When the input and output value types are different. 
For example, consider writing a transformation that finds all the unique strings corresponding to each key. One way would be to use map to transform each element into a Set and then combine the Sets with reduceByKey:
rdd.map(kv => (kv._1, new Set[String]() + kv._2))
    .reduceByKey(_ ++ _)
This code results in tons of unnecessary object creation because a new set must be allocated for each record. It’s better to use aggregateByKey, which performs the map-side aggregation more efficiently:

val zero = new collection.mutable.Set[String]()
rdd.aggregateByKey(zero)(
    (set, v) => set += v,
    (set1, set2) => set1 ++= set2)
- Avoid the flatMap-join-groupBy pattern. 
When two datasets are already grouped by key and you want to join them and keep them grouped, you can just use cogroup. That avoids all the overhead associated with unpacking and repacking the groups.

2. When Shuffles Don’t Happen
It’s also useful to be aware of the cases in which the above transformations will not result in shuffles. Spark knows to avoid a shuffle when a previous transformation has already partitioned the data according to the same partitioner. Consider the following flow:

rdd1 = someRdd.reduceByKey(...)
rdd2 = someOtherRdd.reduceByKey(...)
rdd3 = rdd1.join(rdd2)
Because no partitioner is passed to reduceByKey, the default partitioner will be used, resulting in rdd1 and rdd2 both hash-partitioned. These two reduceByKeys will result in two shuffles. If the RDDs have the same number of partitions, the join will require no additional shuffling. Because the RDDs are partitioned identically, the set of keys in any single partition of rdd1 can only show up in a single partition of rdd2. Therefore, the contents of any single output partition of rdd3 will depend only on the contents of a single partition in rdd1 and single partition in rdd2, and a third shuffle is not required.

3. When More Shuffles are Better
An extra shuffle can be advantageous to performance when it increases parallelism. For example, if your data arrives in a few large unsplittable files, the partitioning dictated by the InputFormat might place large numbers of records in each partition, while not generating enough partitions to take advantage of all the available cores. In this case, invoking repartition with a high number of partitions (which will trigger a shuffle) after loading the data will allow the operations that come after it to leverage more of the cluster’s CPU.

4. Secondary Sort
Another important capability to be aware of is the repartitionAndSortWithinPartitions transformation.
> https://blog.csdn.net/u010003835/article/details/101000077
- Partitioner的传入实现；Ordering.by的排序算法；
- 同时完成分区和排序功能，而且还可以实现二次排序.

## Resource tuning & configuring
- Every Spark executor in an application has the same fixed number of cores and same fixed heap size. The number of cores can be specified with the **--executor-cores** (spark.executor.cores) flag when invoking spark-submit. 
Similarly, the heap size can be controlled with the **--executor-memory** (spark.executor.memory) property. 
The cores property controls the number of concurrent tasks an executor can run. --executor-cores 5 means that each executor can run a maximum of five tasks at the same time. 
The memory property impacts the amount of data Spark can cache, as well as the maximum sizes of the shuffle data structures used for grouping, aggregations, and joins.

- The **--num-executors** (spark.executor.instances) configuration property control the number of executors requested. 
You will be able to avoid setting this property by turning on dynamic allocation with the spark.dynamicAllocation.enabled property. Dynamic allocation enables a Spark application to request executors when there is a backlog of pending tasks and free up executors when idle.

- It’s also important to think about how the resources requested by Spark will fit into what YARN has available. 
The relevant YARN properties are:
**yarn.nodemanager.resource.memory-mb** controls the maximum sum of memory used by the containers on each node.
**yarn.nodemanager.resource.cpu-vcores** controls the maximum sum of cores used by the containers on each node.

Asking for five executor cores will result in a request to YARN for five virtual cores. The memory requested from YARN is a little more complex for a couple reasons:
--executor-memory/spark.executor.memory controls the executor *heap size*, but JVMs can also use some memory off heap, for example for interned Strings and direct byte buffers. The value of the **spark.yarn.executor.memoryOverhead** property is added to the executor memory to determine the full memory request to YARN for each executor. It defaults to max(384, .07 * spark.executor.memory).
YARN may round the requested memory up a little. YARN’s **yarn.scheduler.minimum-allocation-mb** and **yarn.scheduler.increment-allocation-mb** properties control the minimum and increment request values respectively.

- A few final concerns when sizing Spark executors:
1. The application master, which is a non-executor container with the special capability of requesting containers from YARN, takes up resources of its own that must be budgeted in. In yarn-client mode, it defaults to a 1024MB and one vcore. In yarn-cluster mode, the application master runs the driver, so it’s often useful to bolster its resources with the **--driver-memory** and --driver-cores properties.
2. Running executors with too much memory often results in excessive garbage collection delays. **64GB** is a rough guess at a good upper limit for a single executor.
3. I’ve noticed that the HDFS client has trouble with tons of concurrent threads. A rough guess is that at most **five** tasks per executor can achieve full write throughput, so it’s good to keep the number of cores per executor below that number.
4. Running **tiny executors** (with a single core and just enough memory needed to run a single task, for example) throws away the benefits that come from running multiple tasks in a single JVM. For example, broadcast variables need to be replicated once on each executor, so many small executors will result in many more copies of the data.

## Tuning parallelism
- 一个手动设定资源参数的例子
a worked example of configuring a Spark app to use as much of the cluster as possible: Imagine a cluster with six nodes running NodeManagers, each equipped with 16 cores and 64GB of memory. 
A better option would be to use --num-executors 17 --executor-cores 5 --executor-memory 19G. Why?
1. 根据HDFS的写入现在设定--executor-cores为5；每节点16核，所以单节点启动3个executor;
2. 每个节点总内存64G - 1G(留给系统运行)=63G; 3个executor，每个的内存=63 / 3 * (1-0.07*(预留给堆外内存的)*) = 19.53；因此设定19G给--executor-memory；
3. 运行的executor的总数量 = 每节点3个 * 6 个节点= 18，再减去一个给driver的资源；因此设定executor总数为17个。

- task数量如何计算
1. The number of tasks in a stage is the same as the number of partitions in the last RDD in the stage. 
The number of partitions in an RDD is the same as the number of partitions in the RDD on which it depends, 
with a couple exceptions: 
the coalesce transformation allows creating an RDD with fewer partitions than its parent RDD, 
the union transformation creates an RDD with the sum of its parents’ number of partitions, 
and cartesian creates an RDD with their product.
2. RDDs produced by textFile or hadoopFile have their partitions determined by the underlying MapReduce InputFormat that’s used. Typically there will be a partition for each HDFS block being read. 
Partitions for RDDs produced by parallelize come from the parameter given by the user, or spark.default.parallelism if none is given.

3. A small number of tasks also mean that more memory pressure is placed on any aggregation operations that occur in each task. Any join, cogroup, or *ByKey operation involves holding objects in hashmaps or in-memory buffers to group or sort. 
When the records destined for these aggregation operations do not easily fit in memory, some mayhem can ensue. 
- First, holding many records in these data structures puts pressure on garbage collection, which can lead to pauses down the line. 
- Second, when the records do not fit in memory, Spark will spill them to disk, which causes disk I/O and sorting. This overhead during large shuffles is probably the number one cause of job stalls I have seen at Cloudera customers.

4. So how do you increase the number of partitions?
- If the stage in question is reading from Hadoop, your options are:
    Use the repartition transformation, which will trigger a shuffle.
    Configure your InputFormat to create more splits.
    Write the input data out to HDFS with a smaller block size.
- If the stage is getting its input from another stage, 
the transformation that triggered the stage boundary will accept a numPartitions argument, such as
val rdd2 = rdd1.reduceByKey(_ + _, numPartitions = X)
What should “X” be? 
The most straightforward way to tune the number of partitions is experimentation: 
Look at the number of partitions in the parent RDD and then keep multiplying that by 1.5 until performance stops improving.


## Representing the data
- Tuning Data Structures
The first way to reduce memory consumption is to avoid the Java features that add overhead, such as pointer-based data structures and wrapper objects. There are several ways to do this:
Design your data structures to prefer arrays of objects, and primitive types, instead of the standard Java or Scala collection classes (e.g. HashMap). The fastutil library provides convenient collection classes for primitive types that are compatible with the Java standard library.
Avoid nested structures with a lot of small objects and pointers when possible.
Consider using numeric IDs or enumeration objects instead of strings for keys.
If you have less than 32 GB of RAM, set the JVM flag -XX:+UseCompressedOops to make pointers be four bytes instead of eight. You can add these options in spark-env.sh.

- Disk data
Whenever you have the power to make the decision about how data is stored on disk, use an extensible binary format like Avro, Parquet, Thrift, or Protobuf. Pick one of these formats and stick to it.

- Slimming Down
Data flows through Spark in the form of records. 
A record has two representations: 
a deserialized Java object representation and a serialized binary representation. 
In general, Spark uses the deserialized representation for records in memory and the serialized representation for records stored on disk or being transferred over the network. 
There is work planned to store some in-memory shuffle data in serialized form.
The spark.serializer property controls the serializer that’s used to convert between these two representations. The Kryo serializer, org.apache.spark.serializer.KryoSerializer, is the preferred option.