

### 线程和进程
- 进程是程序的启动，同时启动多个进程的性能消耗较大；
- 线程是在进程内启动的，并发启动消耗较少，线程间共享内存，和轮询CPU资源，当然，多核情况下，可以实现多线程并发执行。


### 线程的生命周期
- new(新建) Thread t1 = new Thread()
- runnuable(可运行) t1.start() 根据优先级 排队等待CPU资源获取
- running(运行) 获取的CPU资源 执行#run()方法，直到执行结束终止
- dead(终止) 分为：自然终止和异常终止，自然终止是指执行完成，自然return返回；异常终止是指被stop等暴力终止；
- blocked(阻塞) 是指基于一些原因让线程让出CPU，暂停执行，进入阻塞状态；
直到条件解除，线程重新进入runnable状态后，获取CPU资源，然后执行进入running状态；
阻塞分为3中类型：
1. 睡眠 #sleep(long t), 睡眠结束，即进入runnable
2. 等待 #wait(), 等待调用方的notify()方法
3. 阻塞 #suspend(), 等待resume()方法

### 线程的创建启动
1. 继承Thread类，复写run()方法; 调取start()方法；
2. 实现Runnable接口，实现run()方法
3. 实现Callable<T>接口，实现里面的call()方法，通过FutureTask进行调用；
一个调取的例子
```java
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        return UUID.randomUUID().toString().substring(0,8);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //调取callable
        FutureTask<String> futureTask = new FutureTask<>(new MyCallable());
        //启动线程
        new Thread(futureTask,"future").start();
        //获取线程执行结果
        String result = futureTask.get();
        System.out.println(result);
    }
}
```

### 使用线程池的创建
- 避免使用Executors的fix或者cached进行创建，最好使用ThreadPoolExecutor来进行创建；
- 因为fix会默认用MAX_VALUE的queue，cached会创建MAX_VALUE的thread.

```java
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {
    public static final int POOL_NUM = 10;

    public static void main(String[] args) throws InterruptedException {
        //初始化线程池 需要指定线程池大小，排队队列，拒绝策略
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 10,
                1L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        //依次插入线程到线程池
        for (int i = 0; i < POOL_NUM; i++) {
            ThreadRunnableAtTp runnableAtTp = new ThreadRunnableAtTp(String.valueOf(i));
            executor.execute(runnableAtTp);
        }
        executor.awaitTermination(1,TimeUnit.SECONDS);
        executor.shutdown();
    }
}

class ThreadRunnableAtTp implements Runnable {
    String name = "";
    ThreadRunnableAtTp(String threadName){
        name = threadName;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("Thread num is:"+name+" "+Thread.currentThread()+",count:"+i);
        }
    }
}
```

### 线程的中断终止
1. 使用interrupt(),来标识线程终止；
2. 使用interrupted()和isInterrupted()来判断是否终止，根据是否被终止，觉得本线程是否可以退出，终止等操作；

```java
public class InterruptThread {

    public static void main(String[] args) {
        Thread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(20);
            //标识中断位
            thread.interrupt();
            //使用isInterrupted判断的是thread线程的中断位，而且不擦洗；所以显示的是两个ture的结果
            System.out.println("runner stop 1:"+thread.isInterrupted());
            System.out.println("runner stop 2:"+thread.isInterrupted());

            //使用interrupted判断的是调取线程即main线程的中断位，没有main的中断，所以两次都是false
            System.out.println("caller stop 1:"+thread.interrupted());
            System.out.println("caller stop 2:"+thread.interrupted());

            //进行main线程的中断操作
            Thread.currentThread().interrupt();
            //使用interrupted判断的是调取线程即main线程的中断位，而且会擦洗；所以显示的是第一次ture，第二次false的结果
            System.out.println("caller stop 3:"+thread.interrupted());
            System.out.println("caller stop 4:"+thread.interrupted());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done");
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        super.run();
        try {
            for (int i = 0; i < 50000; i++) {
                //判断是否被中断过
                if(this.isInterrupted()) {
                    System.out.println("Thread is down!");
                    //判断后可以break跳出或者直接抛异常，或者直接return
//                    break;
                    throw new InterruptedException("be interrupted!");
                }
                System.out.println("i="+i);
            }
        }catch (InterruptedException e){
            //当然也可以捕捉中断异常
            System.out.println("get exception catch");
        }
    }
}
```

