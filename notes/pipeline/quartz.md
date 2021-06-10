

## Quartz的使用必须掌握下面三个对象

- Scheduler 定时器对象
即改scheduler实例，由StdSchedulerFactory工厂类，读入配置进行创建；
Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
或者，读取配置信息：
schedulerFactory.initialize(properties);
scheduler = schedulerFactory.getScheduler();

- JobDetail 任务对象
定义一个任务具体的ID和group，以及具体要做的事情；
有JobBuilder来创建，具体例子: 
JobDetail jobDetail = JobBuilder.newJob(HelloJob.class).withIdentity("job41","group1").build();
具体作业内容由HelloJob.class实现Job接口里面的excute()来override实现；

- Trigger 触发器对象
定义具体何时如何周期运行的问题:
比如，使用CornTrigger任务调度触发器的例子:
Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1","group1")
.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")).build();

- 最后scheduleJob方法，可以将scheduler，jobDetail，trigger三者串起来；
scheduler.scheduleJob(jobDetail, trigger);

- 最后的最后:
通过该实例scheduler的start()方法，既可以启动定时调度流程。

