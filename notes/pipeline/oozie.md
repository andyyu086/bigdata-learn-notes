## Oozie的简单使用

### 简单使用记录
- 主要是两个配置文件：job.properties和workflow.xml；
- 其中，job.properties中配置nameNode，jobTracker，以及workflow.xml的位置
- workflow.xml主要配置workflow和action，基本上一个任务配置一个action内，
主要3个节点start、kill、和end；此外每个action的结果ok或者error，ok对应to属性指定下一步action；
这样可以把整个workflow串起来；
- 此外，对于希望定期跑的任务可以使用coordinator.xml，在里面配置好具体频次和调取的子workflow路径；
- 具体命令栏执行任务的命令是：
> oozie job -oozie http://hh:11000/oozie -config ./job.properties -run
