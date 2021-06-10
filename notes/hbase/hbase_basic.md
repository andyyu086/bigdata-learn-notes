## Hbase基础

### 数据模型
- Name space
相当于关系数据库的database，默认具有一个hbase和default数据库，default为用户空间默认的数据库；
- Region
region由表的若干行组成，根据rowkey字典排序；
一个region只能在一个region server上；当region过大时，会触发region split操作；

- Row
每行数据由rowkey和多个column组成；数据按照rowkey字典顺序存储；
只能通过rowkey进行检索，所以说rowkey的设计非常重要。

- Column
每个列由column family和column qualifier进行限定；具体建表时可以只预定义column family.

- Time Stamp
用于表示数据的版本，如果不指定写入的话，默认写为hbase操作的时间。

- Cell
由 rowkey cf cq ts 四者唯一确定一个cell，cell内的数据没有类型，全部由字节码形式存储。

### 基本架构
- 基本部件图：

Client --> ZK <--> HMaster
|                    |
V                    V
------RegionServer------------------------
HLog(WAL)
HRegion -> Store(Mem Store) -> Stroe File(HFile) -> HDFS client
\-------------------------------------------

- Region Server
Region Server对region进行管理，包括两个方面：
数据操作方面： get put delete
region操作方面：split region和compact region

- HMaster
主要对region server进行管理，包括两个方面：
对于数据表的操作方面：create delte alter
对于region server的管理方面： 分配region给region server；监控region server的状态，实现负载均衡和故障迁移。

- Mem Store
缓存设置， 由于Hfile需要数据有序，写数据时先写到mem store，然后等到刷数据的时机，再将缓存数据刷入Hfile。

- Store File
实际存储在HDFS的Hfile文件，一个Store可能会有多个Hfile，单个Hfile内的数据是有序的。

- WAL
由于数据先保存到Mem store，因此可能出现机器故障导致的内存数据丢失问题，因此，写mem store前，先写日志WAL。
故障发生时，可以进行数据的恢复。

### 读写过程
1. 写操作流程
Client 访问ZK获取region server列表，并缓存；
根据rowkey获取到数据所在的具体的region server和region；
跟相应的region server进行通信，建数据顺序写入到WAL；
将数据写入mem store，将数据在mem store内排序；
向客户端发送ack信号；
将mem store等到时机，刷入HFile.

2. Flush数据操作
具体flush的时机的影响的3个因子是：
- mem store达到配置的一个大小阈值:  hbase.hregion.memstore.flush.size
- 时间达到自动刷写的时间间隔： hbase.regionserver.optionalcacheflushinterval
- WAL的挤压的数量达到一个阈值。

3. 读操作流程
client 访问ZK 获取具体rowkey所在region server和region
跟具体region server通信，分别读取Block Cache(缓存)，Mem store和HFile中的数据，将数据合并返回

### Region和Store操作过程
1. Store compaction 操作
由于每次都是基于时间间隔和文件大小顺序写Hfile，可能会出现数据分散在多个小文件中；
为了性能考虑，需要进行小文件的合并compaction操作；
具体compaction操作分minor compaction和major compaction两种；
minor compaction是对相邻的多个小HFile进行合并操作，不会清理过期和删除数据；
major compaction会对整个store下的HFile都进行合并操作，会进行过期和删除数据的清理操作。

2. Region split 操作
如果单region下的store的size超过一个阈值： hbase.hregion.max.filesize 时，会触发region的拆分操作；
hbase会择机将新拆分的region迁移到新的region server，以达成负载均衡。


## 基本表操作语句
1. 查看表清单
> list

2. 建表
create 表名 cf名
> create 'student','info'
create 'patent','pat'

3. 写数据
put 表名 rowkey cf:cloumn value
> put 'student','1001','info:sex','male'

4. 读数据
读取指定行或者指定列
> get 'student','1001'
> get 'student','1001','info:name'

## HBASE JAVA API
- 一个java api的测试类例子:
```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HbaseTest {

    private static Connection connection;

    /**
     * 初始化新建connection，配置ZK的参数
     */
    static {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort","2181");
        configuration.set("hbase.zookeeper.quorum","xx-master");

        try{
            connection = ConnectionFactory.createConnection(configuration);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 传入CF和表名，进行建表
     * @param tableName
     * @param family
     * @return
     */
    public static boolean createTable(String tableName,List<String> family){
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            family.forEach(cf -> {
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
                hColumnDescriptor.setMaxVersions(1);
                tableDescriptor.addFamily(hColumnDescriptor);
            });
            admin.createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * put 写数据操作
     * @param tableName
     * @param rowKey
     * @param cf
     * @param qualifier
     * @param value
     * @return
     */
    public static boolean putRow(String tableName,String rowKey,String cf,String qualifier,String value){
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(cf),Bytes.toBytes(qualifier),Bytes.toBytes(value));
            table.put(put);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据rowkey进行get读操作
     * @param tableName
     * @param rowKey
     * @return
     */
    public static Result getRow(String tableName, String rowKey){
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            return table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final String TABLE_NAME = "patent";
    public static final String TABLE_CF = "pat";

    @Test
    public void createTableTest(){
        List<String> cfs = Arrays.asList(TABLE_CF);
        boolean res = createTable(TABLE_NAME, cfs);
        System.out.println("create result is: "+res);
    }

    @Test
    public void putTest(){
        boolean res = putRow(TABLE_NAME, "pat_patent_lit_7610688485615_956", TABLE_CF, "lit", "test");
        System.out.println("create result is: "+res);
    }

    @Test
    public void getTest(){
        Result res = getRow(TABLE_NAME, "pat_patent_lit_7610688485615_956");

        if(res != null){
            String lit = Bytes.toString(res.getValue(Bytes.toBytes(TABLE_CF), Bytes.toBytes("lit")));
            System.out.println(lit);
        }
    }
}
```

## 配合flink
- 一个实现flink sink，将flink计算的结果数据写入hbase的例子:

```scala
package com.bigdata.flink.sink

import java.util.{Random, StringJoiner}

import com.bigdata.flink.entity.PatLit
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes

class HbaseSink(tableName:String,family:String) extends RichSinkFunction[PatLit]{

  val MS_MAX_VALUE = 9223372036854L

  var conn:Connection = _
  var table:Table = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    val conf = HBaseConfiguration.create()
    conf.set(HConstants.ZOOKEEPER_QUORUM,"xx-master")
    conf.set(HConstants.ZOOKEEPER_CLIENT_PORT,"2181")
    conn = ConnectionFactory.createConnection(conf)
    table = conn.getTable(TableName.valueOf(tableName))
  }

  def covByte(str: String) = {
    Bytes.toBytes(str)
  }

  def addHbaseColumn(put: Put, patLit: PatLit): Unit = {
    put.addColumn(covByte(family),covByte("val"),covByte(patLit.getVal()))
    put.addColumn(covByte(family),covByte("id"),covByte(patLit.getId()))
  }

  def makeRowKey(ts: Long) = {
    val random = new Random
    val x: Int = random.nextInt(900) + 100

    val sj: StringJoiner = new StringJoiner("_")
    sj.add("pat_patent_lit")
      .add(String.valueOf(MS_MAX_VALUE - ts))
      .add(String.valueOf(x)).toString
  }

  override def invoke(patLit: PatLit, context: SinkFunction.Context[_]): Unit = {
    val put = new Put(covByte(makeRowKey(patLit.getTs())))
    addHbaseColumn(put,patLit)
    table.put(put)
  }

  override def close(): Unit = {
    table.close()
    conn.close()
  }
}
```

## Hbase性能调优

### 预分区
对每个region，设置start row和end row的范围，建表的时候，传入分区值，设定好region的分布；

### row key设计
主要设计目标是根据row key，把数据均匀的分布到region server上，避免数据倾斜的问题；
rowkey设计遵循的几点
- 字符串类型
- 强业务唯一性，具有业务的明确意义的唯一性标识
- 有序性，尽量让最近最热的数据可以快速获取到，基于字典序排序，设置Long.MAX_VALUE - timestamp
- 定长性，为了保证有序的；此外长度方面最好设置为8的倍数；而且，控制长度不要太长。
- 散列分布，必要时加入一下随机数，避免热数据集中到单region server。

### HDFS参数优化
1. 属性：dfs.support.append
解释：开启 HDFS 追加同步，可以优秀的配合 HBase 的数据同步和持久化。默认值为 true。

2. 数据的写入压缩
mapreduce.map.output.compress
mapreduce.map.output.compress.codec
解释：开启这两个数据可以大大提高文件的写入效率，减少写入时间。
第一个属性值修改为true，
第二个属性值修改为：org.apache.hadoop.io.compress.GzipCodec或者其他压缩方式。

### HBASE参数优化
1. region split
参数hbase.hregion.max.filesize HFile大于这个值被split；如果默认10GB太大，可以适当调小。

2. flush
hbase.hregion.memstore.flush.size参数 调整进行flush操作的时机。

## Row Key设置参考
https://help.aliyun.com/document_detail/59035.html?spm=a2c4g.11186623.6.861.75b259fdh2uwsw


## HUE 连接使用hbase
https://blog.csdn.net/lvtula/article/details/89707937

## Prefix filter
scan 'patent',{FILTER=>"PrefixFilter('patent_e644c54e')"}

## 官方文档参考
https://hbase.apachecn.org/#/docs/1


## 查询条数
hbase查询条数：
count 'oms:package_current_status',INTERVAL => 5000000,CACHE => 5000000

表大小可以通过  hdfs dfs -du -h /hbase/data/default/表名查看

## 和HIVE的集成
```shell
[hadoop@ip-xx]$ hive

Logging initialized using configuration in file:/etc/hive/conf.dist/hive-log4j2.properties Async: true
hive> set hbase.zookeeper.quorum=ec2-52-xx.compute-1.amazonaws.com;
hive> create external table inputTable (key string, value string)
    >      stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    >       with serdeproperties ("hbase.columns.mapping" = ":key,pat:img")
    >       tblproperties ("hbase.table.name" = "patent");
OK
Time taken: 3.907 seconds
hive> select count(key) from inputTable;
Query ID = hadoop_20210429030834_99579b78-1975-405d-8168-4b692660f61d
Total jobs = 1
Launching Job 1 out of 1
Status: Running (Executing on YARN cluster with App id application_1619664174448_0002)

----------------------------------------------------------------------------------------------
        VERTICES      MODE        STATUS  TOTAL  COMPLETED  RUNNING  PENDING  FAILED  KILLED  
----------------------------------------------------------------------------------------------
Map 1 .......... container     SUCCEEDED     12         12        0        0       0       0  
Reducer 2 ...... container     SUCCEEDED      1          1        0        0       0       0  
----------------------------------------------------------------------------------------------
VERTICES: 02/02  [==========================>>] 100%  ELAPSED TIME: 54.83 s    
----------------------------------------------------------------------------------------------
OK
12734956
Time taken: 65.934 seconds, Fetched: 1 row(s)
hive> 

```


