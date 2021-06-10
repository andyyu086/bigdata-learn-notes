
# 四种拒绝策略
样例代码: 使用MyRunnable创建10个线程来执行，具体的差别在main的comment里面有表述。

```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RejectedPolicyRun {
    public static final int THREAD_SIZE = 1;
    public static final int CAPACITY = 1;

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_SIZE,THREAD_SIZE,0,TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(CAPACITY));
        //前两个task执行，后面的task都废弃
//        executeMeth(executor,"discard");
        //第一个和最后一个task执行，中间的task都废弃；因为使用了oldest丢弃方式
//        executeMeth(executor,"oldest");
        //前两个task执行，第三个task开始抛异常
//        executeMeth(executor,"rejected");
        //
        executeMeth(executor,"caller");
    }

    private static void executeMeth(ThreadPoolExecutor executor, String discard) {
        switch (discard){
            case "discard":
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                break;
            case "oldest":
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                break;
            case "rejected":
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
                break;
            case "caller":
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                break;
            default:
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                break;
        }
        for (int i = 0; i < 10; i++) {
            Runnable myrun = new MyRunnable("task-"+i);
            executor.execute(myrun);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

```
```java
import java.util.Random;

public class MyRunnable implements Runnable{
    private String name;

    public MyRunnable(String name){
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(this.name + " is running. by "+Thread.currentThread().getName());
        try {
            Random random = new Random();
            int d = 2 + random.nextInt(5);
            Thread.sleep(d * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```