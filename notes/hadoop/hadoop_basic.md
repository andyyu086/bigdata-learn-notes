## BigDataPlatform 简单记录

### MR离线相关

#### 和Hbase对接
- MR中对接Hbase可以使用TableMapReduceUtil.initTable**Reducer**Job 将结果写入Hbase；
使用TableMapReduceUtil.initTable**Mapper**Job 将Hbase数据读入MR；

