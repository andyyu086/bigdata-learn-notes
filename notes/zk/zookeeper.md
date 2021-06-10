## 基本描述

### 安装

#### 下载
```shell
# wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz
```
#### 配置
- 解压后，对conf文件夹里面的拷贝创建zoo.cfg文件
```shell
[conf]# grep -v '#' zoo.cfg 
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/data/opt/zk/zookeeper
clientPort=2181
server.1=192.168.3.58:2888:3888
server.2=192.168.3.57:2888:3888
server.3=192.168.3.56:2888:3888
```
>配置参数说明：
>
>- **tickTime**：用于计算的基础时间单元。比如 session 超时：N*tickTime；
>- **initLimit**：用于集群，允许从节点连接并同步到 master 节点的初始化连接时间，以 tickTime 的倍数来表示；
>- **syncLimit**：用于集群， master 主节点与从节点之间发送消息，请求和应答时间长度（心跳机制）；
>- **dataDir**：数据存储位置；
>- **clientPort**：用于客户端连接的端口，默认 2181
>- **server.1**：用于定义服务器和端口，其中的".1"表示为节点标识，其中的1是“myid”文件里面的定义序号；
每个节点的id唯一，myid文件保存在**dataDir**定义的目录下，每个节点需要手动生成myid文件；同时拷贝zookeeper的配置和安装包；

#### 启动
- 使用bin内的脚本启动,需要在所有节点执行
```shell
bin/zkServer.sh start
```
- 最后，使用`zkServer.sh status` 查看集群各个节点状态，其中会有一个leader节点，其他的为follower节点。

