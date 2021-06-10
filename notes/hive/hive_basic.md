
### hive启动
https://www.jianshu.com/p/7b1b21bf05c2
- 主要执行脚本命令为:
nohup hive --service metastore  2>&1 &
nohup  hive --service hiveserver2   2>&1 &

### 使用Dbeaver连接
https://www.cnblogs.com/huojinfeng/p/12869469.html


### Spark链接Hive
- 注意使用hadoop-client 和mysql connector pom依赖
- Spark 2.0版本以上 不要拷贝core-site.xml和hdfs-site.xml （非必须，对某些版本来说只需要hive-site.xml!!!
- 要不然 会报lzo的错误
https://blog.csdn.net/weixin_43825553/article/details/89249695

### spark 执行hql示例代码
```python
import org.apache.spark.sql.SparkSession

object SparkConHive {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("sp2hive")
      .master("local[*]")
      .enableHiveSupport()
      .getOrCreate()
    val sql1 = "use szt"
    spark.sql(sql1)
    val sql2 = "show tables"
    spark.sql(sql2).show()
    val sql3 = "SELECT station,\n       collect_list(deal_date),\n       " +
      "collect_list(card_no),\n       collect_list(company_name),\n       collect_list(equ_no),\n       " +
      "count(*) c\nFROM dwd_fact_szt_in_detail\nWHERE DAY = '2018-09-01'\nGROUP BY station\nORDER BY c DESC"
    spark.sql(sql3).show()
  }
}
```