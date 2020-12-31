
## 安装
https://www.jianshu.com/p/db9f37bb7f98

## Kakfa Eagle
- 需要配置开通JMS Port才可以访问
- 使用 KSQL 注意topic名包含横线时解析有问题：成功的语句例子如下：
> select * from topic_flink_szt_new where `partition` in (0) limit 10

## 启动
- nohup sh ke.sh start &
