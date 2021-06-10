
## 基本安装文档
- Linux环境 直接使用yum安装
https://clickhouse.tech/docs/zh/getting-started/install/
https://www.jianshu.com/p/5f5ee0904bba


## 问题解决文档
https://www.jianshu.com/p/9498fedcfee7

## 连接DBeaver
https://www.jianshu.com/p/5b8ccf9da114

## 操作
- 手动启动
sudo -u clickhouse clickhouse-server --config-file=/etc/clickhouse-server/config.xml
- 服务关闭
sudo -u clickhouse clickhouse-server stop
- 查看日志
cd /etc/clickhouse-server/
tail clickhouse-server.err.log 

- 手动停止
sudo -u clickhouse clickhouse-server stop --config-file=/etc/clickhouse-server/config.xml

- 开机启动关闭
systemctl status clickhouse-server
systemctl is-enabled clickhouse-server
systemctl disable clickhouse-server
