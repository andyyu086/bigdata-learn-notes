nohup spark-submit --master yarn  --deploy-mode cluster 
		--jars /home/xx/lib/mysql-connector-java-5.1.32-bin.jar  --class xxx.xx.xx 
		--name XX 
		--driver-memory 2g 
		--driver-cores 2 
		--executor-memory 2g 
		--executor-cores 2  
		--num-executors 30  
		--conf spark.default.parallelism=300  
		xx-0.0.1-SNAPSHOT.jar  > xx.log  &
 
	参数解析：
		1.8台主机，每台主机有2个cpu和62G内存，每个cpu有8个核，那么每台机器一共有16核，8台机器一共有128核
		2.num-executors 30 和 executor-cores 2：每个executors用2个核，那么30个executors一共用60个核
		3.num-executors 30 和 executor-memory 2g：每个executors用2g，那么30个executors一共用60g
		4.每个主机的executors数量 = 30 / 8 约等于 3.75
		5.spark.default.parallelism：指定并行的task数量为300
			参数说明：该参数用于设置每个stage的默认task数量。这个参数极为重要，如果不设置可能会直接影响你的Spark作业性能。
			参数调优建议：Spark作业的默认task数量为500~1000个较为合适。很多同学常犯的一个错误就是不去设置这个参数，
						  那么此时就会导致Spark自己根据底层HDFS的block数量来设置task的数量，默认是一个HDFS block对应一个task。
						  通常来说，Spark默认设置的数量是偏少的（比如就几十个task），如果task数量偏少的话，就会导致你前面设置好的Executor的参数都前功尽弃。
						  试想一下，无论你的Executor进程有多少个，内存和CPU有多大，但是task只有1个或者10个，那么90%的Executor进程可能根本就没有task执行，
						  也就是白白浪费了资源！因此Spark官网建议的设置原则是，设置该参数为num-executors * executor-cores的2~3倍较为合适，
						  比如Executor的总CPU core数量为300个，那么设置1000个task是可以的，此时可以充分地利用Spark集群的资源。
				  
num-executors
	参数说明：该参数用于设置Spark作业总共要用多少个Executor进程来执行。
			  Driver在向YARN集群管理器申请资源时，YARN集群管理器会尽可能按照你的设置来在集群的各个工作节点上，启动相应数量的Executor进程。
			  这个参数非常之重要，如果不设置的话，默认只会给你启动少量的Executor进程，此时你的Spark作业的运行速度是非常慢的。
	参数调优建议：每个Spark作业的运行一般设置50~100个左右的Executor进程比较合适，设置太少或太多的Executor进程都不好。
				  设置的太少，无法充分利用集群资源；设置的太多的话，大部分队列可能无法给予充分的资源。
				  
executor-cores
	参数说明：该参数用于设置每个Executor进程的CPU core数量。这个参数决定了每个Executor进程并行执行task线程的能力。
			  因为每个CPU core同一时间只能执行一个task线程，因此每个Executor进程的CPU core数量越多，越能够快速地执行完分配给自己的所有task线程。
	参数调优建议：Executor的CPU core数量设置为2~4个较为合适。
				  同样得根据不同部门的资源队列来定，可以看看自己的资源队列的最大CPU core限制是多少，再依据设置的Executor数量，来决定每个Executor进程可以分配到几个CPU core。
				  同样建议，如果是跟他人共享这个队列，那么num-executors * executor-cores不要超过队列总CPU core的1/3~1/2左右比较合适，也是避免影响其他同学的作业运行。
				  
executor-memory
	参数说明：该参数用于设置每个Executor进程的内存。Executor内存的大小，很多时候直接决定了Spark作业的性能，而且跟常见的JVM OOM异常，也有直接的关联。
	参数调优建议：每个Executor进程的内存设置4G~8G较为合适。但是这只是一个参考值，具体的设置还是得根据不同部门的资源队列来定。
				  可以看看自己团队的资源队列的最大内存限制是多少，num-executors乘以executor-memory，就代表了你的Spark作业申请到的总内存量（也就是所有Executor进程的内存总和），
				  这个量是不能超过队列的最大内存量的。此外，如果你是跟团队里其他人共享这个资源队列，那么申请的总内存量最好不要超过资源队列最大总内存的1/3~1/2，
				  避免你自己的Spark作业占用了队列所有的资源，导致别的同学的作业无法运行。
				  
				  
--driver-memory
	Driver内存，默认1G
 
--driver-cores
	Driver的核数，默认是1。在yarn集群模式下使用
 
====================================================================================================
 
在我们提交spark程序时，应该如何为Spark集群配置 num-executors、executor-memory、execuor-cores 呢？
	Hadoop / Yarn / OS Deamons
		当我们使用像Yarn这样的集群管理器运行spark应用程序时，会有几个守护进程在后台运行，如NameNode，Secondary NameNode，DataNode，JobTracker和TaskTracker等。
		因此，在指定num-executors时，我们需要确保为这些守护进程留下足够的核心（至少每个节点约1 CPU核）以便顺利运行。
	Yarn ApplicationMaster（AM）
		ApplicationMaster负责协调来自ResourceManager的资源，并与NodeManagers一起执行container并监控其资源消耗。
		如果我们在YARN上运行Spark，那么我们需要预估运行AM所需要的资源（至少1024MB和1 CPU核）。
	HDFS吞吐量
		HDFS客户端遇到大量并发线程会出现一些bug。一般来说，每个executors最多可以实现5个任务的完全写入吞吐量，因此最好将每个executors的核心数保持在该数量之下。
	MemoryOverhead
		JVM还需要一些off heap的内存，请参考下图中描绘的Spark和YARN中内存属性的层次结构，
 
 
简单来说，有以下两个公式：
	每个executor从YARN请求的内存 = spark.executor-memory + spark.yarn.executor.memoryOverhead
	spark.yarn.executor.memoryOverhead = Max(384MB, 7% of spark.executor-memory)
	例如当我设置 --executor-memory=20 时，我们实际请求了 20GB + memoryOverhead = 20 + 7% of 20GB = ~23GB。
	运行具有executors较大内存的通常会导致过多的GC延迟。
	运行具有executors较小内存的（例如，1G & 1 CPU core）则会浪费 单个JVM中运行多个任务所带来的优点。
 
	
不同配置的优劣分析
	1.现在，让我们考虑一个具有以下配置的10节点集群，并分析执行程序的不同可能性，核心内存分布：
		10 Nodes 
		每个Node：16 cores、64GB RAM
 
	2.第一种方法：使用较小的executors，每个核心一个executors
		`--num-executors` = `在这种方法中，我们将为每个核心分配一个executor`
						  = `集群的总核心数`
						  = `每个节点的核心数 * 集群的总节点数` 
						  =  16 x 10 = 160
						  
		`--executor-cores`  = 1 (`每个executor分配的核心数目`)
 
		`--executor-memory` = `每个executor分配的内存数`
							= `每个节点内存总数 / 每个节点上分配的executor数`
							= 64GB/16 = 4GB
		分析：
			由于每个executor只分配了一个核，我们将无法利用在同一个JVM中运行多个任务的优点。 
			此外，共享/缓存变量（如广播变量和累加器）将在节点的每个核心中复制16次。 
			最严重的就是，我们没有为Hadoop / Yarn守护程序进程留下足够的内存开销，我们还忘记了将ApplicationManagers运行所需要的开销加入计算。
 
	3.第二种方法：使用较大的executors，每个节点一个executors
		`--num-executors` = `在这种方法中，我们将为每个节点分配一个executor`
						  = `集群的总节点数`
						  = 10
							
		`--executor-cores` = `每个节点一个executor意味着该节点的所有核心都分配给一个执executor`
						   = `每个节点的总核心数`
						   = 16
							 
		`--executor-memory` = `每个executor分配的内存数`
							= `每个节点内存总数数/每个节点上分配的executor数`
							= 64GB/1 = 64GB
 
		分析：
			每个executor都有16个核心，由于HDFS客户端遇到大量并发线程会出现一些bug，即HDFS吞吐量会受到影响。
			同时过大的内存分配也会导致过多的GC延迟。ApplicationManager和守护进程没有计算在内，HDFS的吞吐量会受到影响，并且会导致垃圾结果过多，不好！
 
	4.第三种方法：使用优化的executors
		1.基于上面提到的建议，让我们为每个执行器分配5个核心, 即 --executor-cores = 5（用于良好的HDFS吞吐量）
		2.为每个节点留出1个核心用于Hadoop / Yarn守护进程, 即每个节点可用的核心数为 16 -1 = 15。 因此，群集中核心的可用总数剩余 15 x 10 = 150
		3.可用executors的数量 =（群集中核心的可用总数/每个executors分配的核心数）= 150/5 = 30
		  然后为ApplicationManager预留1个executors的资源，所以即 --num-executors = 29
		  每个节点的executors数目 30/10 = 3
		4.群集中每个节点的可使用的总内存数 64GB - 1GB = 63GB
		  每个executor的内存= 64GB / 3 = 21GB
		  预留的 off heap overhead = 21GB * 7％ 约等于 1.47G
		  所以，实际的--executor-memory = 21 - 1.47G 约等于 19GB
		5.所以，推荐的配置是：29个executors，每个executors 18GB内存，每个executors 5个核心！
			--num-executors = 29
			--executor-cores = 5
			--executor-memory = 18
			
====================================================================================================
属性名称				默认值			含义
spark.cores.max			(infinite)	当运行在一个独立部署集群上或者是一个粗粒度共享模式的Mesos集群上的时候，最多可以请求多少个CPU核心。默认是所有的都能用。
spark.executor.memory	512m		每个处理器可以使用的内存大小，跟JVM的内存表示的字符串格式是一样的(比如： '512m'，'2g')
 
 
executor 数量 = spark.cores.max / spark.executor.cores 
executor 进程的数量 等于 spark程序设置的最大核心数 除以 每个 executor进程分配的 核心数
 
 
比如机器有12cpu和36g内存，那么将会启动12/4=3个executor，每个executor使用4cpu和12g内存，总共占用worker资源12cpu和36g内存
默认情况下，Spark集群下的worker，只会启动一个Executor。
1、设置每个executor使用的cpu数为4
	spark.executor.cores 4
2、设置cpu的最大使用数量 
	spark.cores.max 12  
3、设置每个executor的内存大小为12gg
	spark.executor.memory 12g
 
Spark1.6的源码部分为：
	protected final String EXECUTOR_MEMORY = "--executor-memory";
	protected final String TOTAL_EXECUTOR_CORES = "--total-executor-cores";
	protected final String EXECUTOR_CORES = "--executor-cores";
	
也可以在提交任务的时候添加：
	SparkSubmit 
		--class com.dyq.spark.MyClass 
		--master:spark://master:7077  
		--total-executor-cores 12 
		--executor-cores 24 
		--executor-memory 12g
 
 
Executor是spark任务(task)的执行单元，Executor运行在worker上，但是不等同于worker，实际上Executor是一组计算资源(cpu核心、memory)的集合。
一个worker上的memory、cpu由多个executor共同使用。
 
spark.executor.cores 	顾名思义这个参数是用来指定executor的cpu内核个数，分配更多的内核意味着executor并发能力越强，能够同时执行更多的task
spark.cores.max  		为一个application分配的最大cpu核心数，如果没有设置这个值默认为spark.deploy.defaultCores
spark.executor.memory	配置executor内存大小
 
executor个数 = spark.max.cores / spark.executor.cores
	集群的executor个数由spark.max.cores、spark.executor.cores共同决定，注意在standalone、mesos coarse-grained模式 下cores不要大于对应节点的内核数
	要保证每个worker上启动的executor均衡。如果不均衡的话会造成数据倾斜，拉慢任务的整体速度。
	在运行过程中一个task对应一个partition，配置不均衡，会导致每个节点处理的任务量不一样，因此会产生短板效应。
	如果运行过程中发现GC时间变红（管理界面可以看到），应该适当调大spark.executor.memory
 
====================================================================================================
 
 
1.RDD在计算的时候，RDD中的每个分区都会起一个task，所以rdd中的分区数目决定了总的的task数目。
2.申请的计算节点Executor数目和每个计算节点核数，决定了你同一时刻可以并行执行的task数目。
3.比如的RDD中有100个分区，那么计算的时候就会生成100个task，你的资源配置为10个计算节点Executor数目，每个计算节点Executor有两2个核，
  同一时刻可以并行的task数目为20，计算这个RDD就需要5轮。
4.如果计算资源不变，你有101个task的话，就需要6个轮次，在最后一轮中，只有一个task在执行，其余核都在空转。
5.如果资源不变，你的RDD只有2个分区，那么同一时刻只有2个task运行，其余18个核空转，造成资源浪费。这就是在spark调优中，增大RDD分区数目，增大任务并行度的做法。
 
 
指定spark executor 数量的公式
	executor数量 = spark.cores.max/spark.executor.cores
	spark.cores.max 是指你的spark程序需要的总核数
	spark.executor.cores 是指每个executor需要的核数
 
指定并行的task数量
	spark.default.parallelism
	参数说明：该参数用于设置每个stage的默认task数量。这个参数极为重要，如果不设置可能会直接影响你的Spark作业性能。
	参数调优建议：Spark作业的默认task数量为500~1000个较为合适。很多同学常犯的一个错误就是不去设置这个参数，
			      那么此时就会导致Spark自己根据底层HDFS的block数量来设置task的数量，默认是一个HDFS block对应一个task。
				  通常来说，Spark默认设置的数量是偏少的（比如就几十个task），如果task数量偏少的话，就会导致你前面设置好的Executor的参数都前功尽弃。
				  试想一下，无论你的Executor进程有多少个，内存和CPU有多大，但是task只有1个或者10个，那么90%的Executor进程可能根本就没有task执行，
				  也就是白白浪费了资源！因此Spark官网建议的设置原则是，设置该参数为num-executors * executor-cores的2~3倍较为合适，
				  比如Executor的总CPU core数量为300个，那么设置1000个task是可以的，此时可以充分地利用Spark集群的资源。
 
在关于spark任务并行度的设置中，有两个参数我们会经常遇到，spark.sql.shuffle.partitions 和 spark.default.parallelism, 那么这两个参数到底有什么区别的？
首先，让我们来看下它们的定义
 						
spark.sql.shuffle.partitions（对sparks SQL专用的设置）	
	默认值：200				
	含义：当shuffle数据时，配置joins联接或aggregations聚合时要使用的分区数 
	
spark.default.parallelism（只有在处理RDD时才会起作用，对Spark SQL的无效）
	默认值：
			1.对于分布式无序移动操作（如reducebykey和join），父RDD中的最大分区数。
			2.对于没有父RDD的并行化操作，它取决于集群管理器：
				1.本地模式：本地计算机上的核心数
				2.细粒模式：8
				3.其他：所有执行器节点上的核心总数或2个，以较大者为准。
	含义：在RDD中，由join、reducebykey、parallelize等转换（如果不是由用户设置）返回的默认分区数 
 
 
命令示例
	spark-submit 
		--class com.cjh.test.WordCount 
		--conf spark.default.parallelism=12 
		--conf spark.executor.memory=800m 
		--conf spark.executor.cores=2 
		--conf spark.cores.max=6 my.jar
		
	spark-submit 	
		--conf spark.sql.shuffle.partitions=20 
		--conf spark.default.parallelism=20
 
====================================================================================================
 
spark.storage.memoryFraction
 
	参数说明：该参数用于设置RDD持久化数据在Executor内存中能占的比例，默认是0.6。也就是说，默认Executor 60%的内存，可以用来保存持久化的RDD数据。
			  根据你选择的不同的持久化策略，如果内存不够时，可能数据就不会持久化，或者数据会写入磁盘。
 
	参数调优建议：如果Spark作业中，有较多的RDD持久化操作，该参数的值可以适当提高一些，保证持久化的数据能够容纳在内存中。
			      避免内存不够缓存所有的数据，导致数据只能写入磁盘中，降低了性能。但是如果Spark作业中的shuffle类操作比较多，而持久化操作比较少，
				  那么这个参数的值适当降低一些比较合适。此外，如果发现作业由于频繁的gc导致运行缓慢（通过spark web ui可以观察到作业的gc耗时），
				  意味着task执行用户代码的内存不够用，那么同样建议调低这个参数的值。
 
spark.shuffle.memoryFraction
 
	参数说明：该参数用于设置shuffle过程中一个task拉取到上个stage的task的输出后，进行聚合操作时能够使用的Executor内存的比例，默认是0.2。
		      也就是说，Executor默认只有20%的内存用来进行该操作。shuffle操作在进行聚合时，如果发现使用的内存超出了这个20%的限制，
			  那么多余的数据就会溢写到磁盘文件中去，此时就会极大地降低性能。
 
	参数调优建议：如果Spark作业中的RDD持久化操作较少，shuffle操作较多时，建议降低持久化操作的内存占比，提高shuffle操作的内存占比比例，
				  避免shuffle过程中数据过多时内存不够用，必须溢写到磁盘上，降低了性能。此外，如果发现作业由于频繁的gc导致运行缓慢，
				  意味着task执行用户代码的内存不够用，那么同样建议调低这个参数的值。
Spark最主要资源管理方式按排名为Hadoop Yarn, Apache Standalone 和Mesos。
在单机使用时，Spark还可以采用最基本的local模式。
目前Apache Spark支持三种分布式部署方式，分别是standalone、spark on mesos和 spark on YARN，其中，第一种类似于MapReduce 1.0所采用的模式，
内部实现了容错性和资源管理，后两种则是未来发展的趋势，部分容错性和资源管理交由统一的资源管理系统完成：让Spark运行在一个通用的资源管理系统之上，
这样可以与其他计算框架，比如MapReduce，公用一个集群资源，最大的好处是降低运维成本和提高资源利用率（资源按需分配）。本文将介绍这三种部署方式，并比较其优缺点。 
 
 
 
1.Standalone模式
	即独立模式，自带完整的服务，可单独部署到一个集群中，无需依赖任何其他资源管理系统。从一定程度上说，该模式是其他两种的基础。借鉴Spark开发模式，
	我们可以得到一种开发新型计算框架的一般思路：先设计出它的standalone模式，为了快速开发，起初不需要考虑服务（比如master/slave）的容错性，
	之后再开发相应的wrapper，将stanlone模式下的服务原封不动的部署到资源管理系统yarn或者mesos上，由资源管理系统负责服务本身的容错。
	目前Spark在standalone模式下是没有任何单点故障问题的，这是借助zookeeper实现的，思想类似于Hbase master单点故障解决方案。
	将Spark standalone与MapReduce比较，会发现它们两个在架构上是完全一致的： 
		1) 都是由master/slaves服务组成的，且起初master均存在单点故障，后来均通过zookeeper解决（Apache MRv1的JobTracker仍存在单点问题，但CDH版本得到了解决）； 
		2) 各个节点上的资源被抽象成粗粒度的slot，有多少slot就能同时运行多少task。不同的是，MapReduce将slot分为map slot和reduce slot，
		   它们分别只能供Map Task和Reduce Task使用，而不能共享，这是MapReduce资源利率低效的原因之一，而Spark则更优化一些，它不区分slot类型，
		   只有一种slot，可以供各种类型的Task使用，这种方式可以提高资源利用率，但是不够灵活，不能为不同类型的Task定制slot资源。总之，这两种方式各有优缺点。 
 
2.Spark On Mesos模式
	这是很多公司采用的模式，官方推荐这种模式（当然，原因之一是血缘关系）。正是由于Spark开发之初就考虑到支持Mesos，因此，目前而言，
	Spark运行在Mesos上会比运行在YARN上更加灵活，更加自然。目前在Spark On Mesos环境中，用户可选择两种调度模式之一运行自己的应用程序
	（可参考Andrew Xia的“Mesos Scheduling Mode on Spark”）： 
		1) 粗粒度模式（Coarse-grained Mode）：每个应用程序的运行环境由一个Dirver和若干个Executor组成，其中，每个Executor占用若干资源，
		   内部可运行多个Task（对应多少个“slot”）。应用程序的各个任务正式运行之前，需要将运行环境中的资源全部申请好，且运行过程中要一直占用这些资源，
		   即使不用，最后程序运行结束后，回收这些资源。举个例子，比如你提交应用程序时，指定使用5个executor运行你的应用程序，每个executor占用5GB内存和5个CPU，
		   每个executor内部设置了5个slot，则Mesos需要先为executor分配资源并启动它们，之后开始调度任务。另外，在程序运行过程中，
		   mesos的master和slave并不知道executor内部各个task的运行情况，executor直接将任务状态通过内部的通信机制汇报给Driver，
		   从一定程度上可以认为，每个应用程序利用mesos搭建了一个虚拟集群自己使用。 
		2) 细粒度模式（Fine-grained Mode）：鉴于粗粒度模式会造成大量资源浪费，Spark On Mesos还提供了另外一种调度模式：细粒度模式，
		   这种模式类似于现在的云计算，思想是按需分配。与粗粒度模式一样，应用程序启动时，先会启动executor，但每个executor占用资源仅仅是自己运行所需的资源，
		   不需要考虑将来要运行的任务，之后，mesos会为每个executor动态分配资源，每分配一些，便可以运行一个新任务，单个Task运行完之后可以马上释放对应的资源。
		   每个Task会汇报状态给Mesos slave和Mesos Master，便于更加细粒度管理和容错，这种调度模式类似于MapReduce调度模式，每个Task完全独立，
		   优点是便于资源控制和隔离，但缺点也很明显，短作业运行延迟大。
 
3.Spark On YARN模式
	这是一种很有前景的部署模式。但限于YARN自身的发展，目前仅支持粗粒度模式（Coarse-grained Mode）。这是由于YARN上的Container资源是不可以动态伸缩的，
	一旦Container启动之后，可使用的资源不能再发生变化，不过这个已经在YARN计划中了。 
	spark on yarn 的支持两种模式： 
		1) yarn-cluster：适用于生产环境； 
		2) yarn-client：适用于交互、调试，希望立即看到app的输出 
	yarn-cluster和yarn-client的区别在于yarn appMaster，每个yarn app实例有一个appMaster进程，是为app启动的第一个container；
	负责从ResourceManager请求资源，获取到资源后，告诉NodeManager为其启动container。yarn-cluster和yarn-client模式内部实现还是有很大的区别。
	如果你需要用于生产环境，那么请选择yarn-cluster；而如果你仅仅是Debug程序，可以选择yarn-client。
 
总结： 
	这三种分布式部署方式各有利弊，通常需要根据实际情况决定采用哪种方案。进行方案选择时，往往要考虑公司的技术路线（采用Hadoop生态系统还是其他生态系统）、
	相关技术人才储备等。上面涉及到Spark的许多部署模式，究竟哪种模式好这个很难说，需要根据你的需求，如果你只是测试Spark Application，你可以选择local模式。
	而如果你数据量不是很多，Standalone 是个不错的选择。当你需要统一管理集群资源（Hadoop、Spark等），那么你可以选择Yarn或者mesos，但是这样维护成本就会变高。 
	· 从对比上看，mesos似乎是Spark更好的选择，也是被官方推荐的 
	· 但如果你同时运行hadoop和Spark,从兼容性上考虑，Yarn是更好的选择。
	· 如果你不仅运行了hadoop，spark。还在资源管理上运行了docker，Mesos更加通用。 
	· Standalone对于小规模计算集群更适合！