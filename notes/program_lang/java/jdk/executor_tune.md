
## 线程池
### 1. 线程假死问题
- 该问题通过连续多天监控，目前发现主要有2个问题原因：
网络异常情况下，通过FtpClient下载的时候，由于网络等原因导致下载异常时，如果没有设置SO_TIMEOUT,会导致线程block住；从而阻塞程序进一步运行:
具体复现，使用命令行:`jstack -l 51`,
打印出各线程的堆栈异常如下:
```
                at sun.security.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:983)
                - locked <0x000000073aedf630> (a java.lang.Object)
                at sun.security.ssl.SSLSocketImpl.readDataRecord(SSLSocketImpl.java:940)
                at sun.security.ssl.AppInputStream.read(AppInputStream.java:105)
                - locked <0x000000073aedfa50> (a sun.security.ssl.AppInputStream)
                at java.io.FilterInputStream.read(FilterInputStream.java:133)
                at java.io.FilterInputStream.read(FilterInputStream.java:107)
                at com.patsnap.common.utils.FtpFileDownloader.download(FtpFileDownloader.java:112)
```
- 爬虫框架线程池设计缺陷
爬虫框架中创建爬取线程池，采用的拒绝策略用的CallerRunsPolicy； 该策略会导致在queue满的情况下，让调用线程自己执行爬虫的；
由于调用线程同时在维护爬取线程池的消息获取和运行等功能；在同时出现SO_TIMEOUT block的时候，就会导致整个服务不消费，从而影响其他数据源的更新。
 
- 解决办法
1. 对于Ftp downloader等基础类，都在底层默认加入了SO_TIMEOUT的参数设置，以避免block的情况发生；
2. 重构优化了该爬虫框架的多线程设计，不再使用CallerRunsPolicy策略，改为AbortPolicy，调取线程和爬虫线程分离，在submit之前，提前判断好queue的排队情况；避免队列满后独立监控队列消费情况，从而保证服务的稳定性。
