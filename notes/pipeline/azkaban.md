## 安装参考官网
1. https://azkaban.github.io/azkaban/docs/latest/#solo-setup
- 去除test环节；使用`./gradlew build installDist -x test` 即可；
- 生成的安装包在:
> azkaban-3.73.1/azkaban-db/build/distributions
azkaban-3.73.1/azkaban-exec-server/build/distributions
azkaban-3.73.1/azkaban-hadoop-security-plugin/build/distributions
azkaban-3.73.1/azkaban-solo-server/build/distributions
azkaban-3.73.1/azkaban-web-server/build/distributions

2. 后续启动步骤参考: https://cloud.tencent.com/developer/article/1491965
> 修改conf/azkaban.properties文件
# 把时区改为上海
default.timezone.id=Asia/Shanghai
直接启动服务
[hadoop@beh07 azkaban-solo-server]$ bin/start-solo.sh
注意：启动服务需要在azkaban-solo-server目录下执行，假如你进入bin目录下执行./start-solo.sh，那么由于配置文件中默认使用的是相对路径，可能会发生找不到文件的错误。

3. job创建和scheduler设置步骤参考
https://blog.csdn.net/chuobu7181/article/details/100681049

4. spark job运行
https://blog.csdn.net/fg19941101/article/details/103380461

5. 相关配置参数参考
https://www.jianshu.com/p/f0126acb4536


