flink问题排查


https://blog.csdn.net/nazeniwaresakini/article/details/107728576

https://blog.csdn.net/penghao_1/article/details/107137579


https://blog.csdn.net/weixin_42993799/article/details/106566037

[{"classification":"flink-conf","properties":{"classloader.resolve-order":"parent-first"}}]

flink run -m yarn-cluster -yn 10 -ytm 1024 -c com.patsnap.flink.oplog.OplogExtractJob ./data-spark-scala-0.0.1-SNAPSHOT-jar-with-dependencies.jar --input s3://data-sync-logs-patsnap-us-east-1/patent/20210415/ --zkSv ec2-18-207-187-170.compute-1.amazonaws.com

