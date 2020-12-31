## Spring 定时任务

### 简单归纳
- 主要就是使用@Scheduled注解配置cron表达式或者fixedRate，fixedDelay进行间隔和延迟操作；
- 此外可以使用@Async 配置异步。
- 代码样例如下：
```java
@Component
public class SpringTaskDemo {
    public static final Logger log = LoggerFactory.getLogger(SpringTaskDemo.class);

    @Async
    @Scheduled(cron = "0/1 * * * * *")
    public void scheduleSync() throws InterruptedException {
        Thread.sleep(3000);
        log.info("Sync run at each second: {}",LocalDateTime.now());
    }

    @Scheduled(fixedRate = 1000)
    public void scheduleRate() throws InterruptedException {
        Thread.sleep(3000);
        log.info("fix rate run at each second: {}",LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 3000)
    public void scheduleDelay() throws InterruptedException {
        Thread.sleep(5000);
        log.info("fix delay run after each second: {}",LocalDateTime.now());
    }
}
```

- 当然不用Spring自带的计划执行功能，也可以用JUC里面的ScheduledExecutorService进行操作。
