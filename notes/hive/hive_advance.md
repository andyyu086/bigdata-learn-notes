
## - Hive原理

### Driver驱动器
1. 解析器 SQL Parser: 将SQL转化为抽象语法树AST；同时进行语法语义解析，判断是否有误；
2. 编译器 Physical Plan: 将AST编译生成逻辑执行计划；
3. 优化器 Query optimizer: 对逻辑执行计划进行优化;
4. 执行器 Execution: 将逻辑计划转换成物理计划，一般是会转化为Hive或者Spark job。


## - 数据类型

### 基本数据类型
Hive数据类型|长度
---|---
TINYINT|1 Byte
SMALLINT|2 Byte
INT|4 Byte
BIGINT|8 Byte

### 集合数据类型
Hive集合数据类型|使用描述
---|---
STRUCT|通过"点号"，进行相应内部字段的获取
MAP|通过"[key]"格式，进行相应键值对，通过key进行获取value
ARRAY|通过"[0]"格式，传入相应索引值，进行相应元素的获取

- 一个分割符设置的例子
```sql
create table test(
name string,
friends array<string>,
children map<string, int>,
address struct<street:string, city:string>
)
row format delimited fields terminated by ','
collection items terminated by '_'
map keys terminated by ':'
lines terminated by '\n';

字段解释：
row format delimited fields terminated by ','  -- 列分隔符
collection items terminated by '_'  --MAP STRUCT 和 ARRAY 的分隔符(数据分割符号)
map keys terminated by ':'				-- MAP中的key与value的分隔符
lines terminated by '\n';					-- 行分隔符
```
- 3种集合类型的获取语句
```sql
select friends[1],children['xiao song'],address.city from test where name="songsong";
```

## DDL

### 分区
- 使用”partitioned by (month string)“建表语句进行分区指定
- 使用”load data local inpath '/opt/module/datas/dept.txt' into table default.dept_partition partition(month='201709');“指定数据导入相应分区
- 使用”partitioned by (month string, day string)“建立二级分区表，相应导入路径设置多个分区列目录

## Query

### Join
- inner join：两侧表都存在的数据，才会select下来；
- left join：以左表为主，左表的所有行都会被select出来，右边表不存在的列值设为null
- right join：和left join类似，不过以右表为主
- full join：两表存在的数据都会被select出来

### case when
- 根据列的值，简单实现if else逻辑，同时可以进行列值的转换。

### order by/sort by
- order by全局单reducer进行排序；sort by单分区内进行排序；
- 通过distribute by配合sort by，可以分区内先进行排序，然后再分区排序；

### 行转列 和 列转行
- 在groupby之后，通过concat或者collect_set等算子，可以实现聚合内的多行数据转化为单列；
- 使用explode可以进行array等复杂列，每个元素unnest为单行视角，变相实现列转行；
一个数据的简单例子：
>《疑犯追踪》 悬疑,动作,科幻,剧情
《Lie to me》 悬疑,警匪,动作,心理,剧情

explode 拆分拍平嵌套array为行视角：
> 《疑犯追踪》 悬疑
《疑犯追踪》 动作
《疑犯追踪》 科幻
《疑犯追踪》 剧情
《Lie to me》 悬疑
《Lie to me》 警匪
《Lie to me》 动作
《Lie to me》 心理
《Lie to me》 剧情

### 窗口函数
- 通过OVER()函数指定分析函数工作的数据窗口大小，这个数据窗口大小可能会随着行的变化而变化；

CURRENT ROW：当前行；
n PRECEDING：往前 n 行数据；
n FOLLOWING：往后 n 行数据；
UNBOUNDED：起点，UNBOUNDED PRECEDING 表示从前面的起点，UNBOUNDED
FOLLOWING 表示到后面的终点；

- 一个简单的实现的例子
```sql
select name,orderdate,cost, 
sum(cost) over() as sample1,--所有行相加 
sum(cost) over(partition by name) as sample2,--按name分组，组内数据相加 
sum(cost) over(partition by name order by orderdate) as sample3,--按name分组，组内数据累加 
sum(cost) over(partition by name order by orderdate rows between UNBOUNDED PRECEDING and current row ) as sample4 ,--和sample3一样,由起点到当前行的聚合 
sum(cost) over(partition by name order by orderdate rows between 1 PRECEDING and current row) as sample5, --当前行和前面一行做聚合 
sum(cost) over(partition by name order by orderdate rows between 1 PRECEDING AND 1 FOLLOWING ) as sample6,--当前行和前边一行及后面一行 
sum(cost) over(partition by name order by orderdate rows between current row and UNBOUNDED FOLLOWING ) as sample7 --当前行及后面所有行 
from business;
```
上述语句获取顾客购买明细，同时进行每个顾客历史下单金额进行sum操作；
通过partition by name之后，对orderdate指定不同的范围，可以实现不同的窗口大小。


## UDF 自定义函数

### UDF
- 主要分三类：
UDF: 一般是单进单出；
UDAF: 一般在聚合下使用，为多进单出；
UTDF: 用于拍平等操作下，例如explode，为单进多出。

- 使用步骤：
1. 继承UDF类，实现具体方法，打包存到hive classpath；
2. 然后，使用 create temporary function udf_lower as 类名，进行函数注册；
3. 最后，在相应SQL就可以直接使用。

## 物理存储 - 压缩

### hive的压缩参数设置
- 可以开启MR的map端和reduce端的压缩启用参数

### 压缩格式比较
[参考这个md文件](../tune/data_format.md)

在实际的项目开发当中，hive表的数据存储格式一般选择：orc或parquet。压缩方式一般选择snappy，lzo。

## 性能调优
1. 本地模式
- 对于比较小的数据量的场景下，设置hive.exec.mode.local.auto=true，可以设置一个数据量大小，默认128MB；
直接本地执行MR，避免分布式下小数据量启动，反而带来耗时很长的问题。

### 表优化
2. 大小表join
- 将key相对分散，并且数据量较小的表放在左边，这样可以大幅降低后面join的数据量导致内存溢出的问题。

3. Map Join
- 将小的维度表直接全部加载到内存DistributeCache，在map端进行join，避免reducer的处理；
- 开启Mapjoin功能，设置set hive.auto.convert.join = true，这样即使大表join小表，由于进行的是map join，性能也不会出现很大的差异。

4. 大表和大表join
- 这种场景下，需要避免单key对应的数据很多，从而发送到相同的reducer中，从而导致内存不足；
- 监控具体各个task的执行时长，找出运行很慢的key，然后分析该key是否是异常数据；
- 如果是异常key，比如说null，判断是否可以进行过滤去除(加 is not null)；
- 是正常的key的话，需要加盐，随机数来增加key的数量，从而降低单key的values量；

比如，表中的id为null是正常数据，使用rand()生成随机key，进行join操作：
```sql
ntable n join ori o on case when n.id is null then concat('hive', rand()) else n.id end = o.id;
```

5. Group By
- 和两个大表join类似，groupby也会出现单key数据量过多，导致的reducer数据倾斜问题；
- 当然，主要思路是减少reducer端进入的数量；尽量在map端进行预聚合，降低数据量之后，再进入reduce端最终处理。
主要的几点调参:
- 设置map端预聚合: hive.map.aggr = true
- 对于数据倾斜发生时，进行负载均衡操作: hive.groupby.skewindata = true
改选项设置为true时，会将倾斜的job，拆分成两个job，第一个job，建原始map结果，**随机分发**到多个reducer(负载均衡)中进行预聚合；
第二个job，将预处理的输出结果，再进行最终的groupby操作。

6. Count distinct
- 由于count distinct操作比较暴力，会将所有数据发送到单个reducer进行处理，如果数据量比较大的话，很容易导致内存溢出；
- 可以采取先进行group by操作获取到所有的key，然后再count distinct操作。

7. 行和列的过滤
- 列过滤
注意只进行需要列的select操作，避免使用"select *";
此外，如果有分区列可以使用，尽量使用分区列进行过滤。
- 行过滤
在join后where进行先定的情况下，会先进行两个表的全部关联，然后where的filter操作；
可以修改为select where子查询之后，再进行join操作，这样可以先filter降低数据量，然后join聚合。

8. 动态分区
设置 hive.exec.dynamic.partition=true

### MR优化
9. map数量
- map的数量，由文件的数量，文件的大小，文件系统block的大小三者决定；
- 如果输入大量小文件，会导致启动的map数过多，这样启动时间大于执行时间，效率较差；
- 如果单文件都正好在127MB，刚好小于block size，这样如果每行数据内容比较少，但是运算逻辑比较复杂，单map的执行时间就会过长；
从而集群并发性能无法发挥，这样的情况下，表现为map数过少；

针对上面的两个问题，对于map数量，要不然调小，要不就是调大。
- map调小
主要是针对小文件，先进行合并操作，hive的配置修改input format: 
set hive.input.format= org.apache.hadoop.hive.ql.io.CombineHiveInputFormat;

- map调大
主要调小 split的 split.maxsize，从而加大split的数量，加大并发；
或者 调整input format为nline input format等其他设置，已降低单map处理数据量，提高任务执行并发。

10. reduce数量
- 对于reduce数量的调整，如果想加大并发：
可以降低每个reducer处理的数据量:
hive.exec.reducers.bytes.per.reducer=256000000
或者直接再job conf设定reduce的数量：
set mapreduce.job.reduces = 15;
- 当然reduce也不是越大越好，也需要考虑启动时间，任务输出数据的大小，数据分布的均匀程度等。

### 严格模式
- 为避免一些恶意的sql，严格模式下对这些sql进行报错处理，以避免大量耗费集群资源。
- 官网配置文件项的解释：
```xml
<property>
    <name>hive.mapred.mode</name>
    <value>strict</value>
    <description>
      The mode in which the Hive operations are being performed. 
      In strict mode, some risky queries are not allowed to run. They include:
        Cartesian Product.
        No partition being picked up for a query.
        Comparing bigints and strings.
        Comparing bigints and doubles.
        Orderby without limit.
    </description>
  </property>
```
- 主要包括：
限制笛卡尔积的使用；
有分区的表查询，需要亲自用分区键查询；
限制没有limit的orderby语句的执行。

### JVM重用
- 对于小task较多的场景，频繁启动jvm性能会较差，可以调整复用的次数:
```xml
<property>
  <name>mapreduce.job.jvm.numtasks</name>
  <value>10</value>
  <description>How many tasks to run per jvm. If set to -1, there is
  no limit. 
  </description>
</property>
```






