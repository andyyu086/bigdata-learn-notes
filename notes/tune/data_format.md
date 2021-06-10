
## 存储格式
### 列式存储格式
Apache Parquet 和 ORC 是针对快速检索数据进行了优化的列式存储格式，用于分析应用程序中。

列式存储格式具有以下特征：
- 按列压缩，针对列数据类型选择压缩算法，可以节省存储空间，并减少查询处理期间的磁盘空间和I/O。
- Parquet and ORC 中的**谓词下推**使得查询可以只提取所需的数据块，从而提高查询性能。当查询从您的数据获取特定列值时，它使用来自数据块谓词的统计信息（例如最大/最小值）来确定读取还是跳过改数据块。
- Parquet和ORC中的数据拆分使得可以将数据读取拆分为多个读进程，在查询处理期间增加并行度。

### ORC: Read by Index
- 每个ORC file由一个或者多个stripe组成，单个stripe的大小一般为256MB；
- 其中的stripe由stripe footer和index data，row data组成；
- 读取顺序为file的file footer，然后是stripe的stripe footer，index data，row data.
- index data，默认1W行一个index,记录行中列的对应在row data中的offset
- row data，相当于row group，先去部分行，然后获取对应的列，对每个列进行encoding后存储，会分成多个stream进行存储。

- 默认情况下，ORC 格式的表是按索引读取的。这是由以下语法定义的：
WITH SERDEPROPERTIES ( 
  'orc.column.index.access'='true')
按索引读取允许您将列重新命名。但这样的设置无法删除列或在表的中间添加列。

### Parquet: Read by Name
- 数据保存为字节数据，按照block大小设置行组的大小；
- 读取顺序为读取footer length，获取文件元数据的offset；然后读取文件元数据中的schema等信息；
- 读取row group的元数据，其中记录了相关列的encoding格式和相关offset。

- 默认情况下，Parquet 格式的表是按名称读取的。这是由以下语法定义的：
WITH SERDEPROPERTIES ( 
  'parquet.column.index.access'='false')
按名称读取允许您在表中间添加列或删除列。但该设置无法将列重新命名。

