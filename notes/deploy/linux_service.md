
## 查看开机启动的情况
> 
启动服务：systemctl start xxx.service
关闭服务：systemctl stop xxx.service
重启服务：systemctl restart xxx.service
显示服务的状态：systemctl status xxx.service
在开机时启用服务：systemctl enable xxx.service
在开机时禁用服务：systemctl disable xxx.service
查看服务是否开机启动：systemctl is-enabled xxx.service
查看已启动的服务列表：systemctl list-unit-files|grep enabled
查看启动失败的服务列表：systemctl --failed

