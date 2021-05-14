
## Synchronized

### 加锁逻辑
1. 使用synchronized进行对于类，对象，对象变量的加锁；
可以对代码段或者方法进行加锁；
2. 如果是对于对象的加锁，new多个对象，会导致加锁失败；
3. 一个执行的例子：
```java
public class SyncThread implements Runnable {
    private static int count;
    private final Integer lock = 1;

    public SyncThread(){
        count = 0;
    }

    public void run() {
//        synchronized (SyncThread.class){
        synchronized (this.lock){
            for (int i = 0; i < 5; i++) {
                try {
                    System.out.println(Thread.currentThread().getName() + ":" + (count++));
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SyncThread syncThread = new SyncThread();
//        Thread th1 = new Thread(new SyncThread(),"th1");
//        Thread th2 = new Thread(new SyncThread(),"th2");

        Thread th1 = new Thread(syncThread,"th1");
        Thread th2 = new Thread(syncThread,"th2");
        th1.start();
        th2.start();
    }
}
```

### 基本原理
- 查看上类的字节码
```
public void run();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=5, locals=5, args_size=1
         0: aload_0
         1: getfield      #3                  // Field lock:Ljava/lang/Integer;
         4: dup
         5: astore_1
         6: monitorenter
         7: iconst_0
         8: istore_2
         9: iload_2

        73: goto          9
        76: aload_1
        77: monitorexit
```
- 主要通过monitorenter进行加锁，保证执行顺序；
- 线程间关系，通过JMM规定的appens-before机制来保证:
具体规则如下：
程序顺序规则：一个线程中的每个操作，happens-before于该线程中的任意后续操作。
监视器锁规则：对一个锁的解锁，happens-before于随后对这个锁的加锁。
**volatile变量**规则：对一个volatile域的写，happens-before于任意后续对这个volatile域的读。
传递性：如果A happens-before B，且B happens-before C，那么A happens-before C。
start()规则：如果线程A执行操作ThreadB.start()（启动线程B），那么A线程的ThreadB.start()操作happens-before于线程B中的任意操作。
join()规则：如果线程A执行操作ThreadB.join()并成功返回，那么线程B中的任意操作happens-before于线程A从ThreadB.join()操作成功返回。
程序中断规则：对线程interrupted()方法的调用先行于被中断线程的代码检测到中断时间的发生。

### CAS
- synchronize悲观锁，每次都认为说可能被人占用；而CAS类似乐观锁的机制，假设说没有人写，直接先读，
读完和预期结果和实际结果比较，不同即重复读取。
- CAS的问题：
1. ABA问题，AtomicStampedReference内来区分；
2. 自旋的时间过长，相当于死循环，对性能消耗较大；
3. 多字段的CAS,通过AtomicReference来进行保证。


