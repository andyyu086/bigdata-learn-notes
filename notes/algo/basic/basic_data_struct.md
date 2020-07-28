# 1. 基础数据类型
## 1.1 String类型
```java
        //赋值，长度，分割等操作
        String s2 = "billformac";
        int length = s2.length();
        s2.substring(4,8);

        //转换为sb，修改和转换等操作
        StringBuilder sb = new StringBuilder(s2.substring(4,8));
        sb.append("bill");
        String sbNew = sb.toString();

        //转换为char array，获取char和index操作
        char[] chars = sbNew.toCharArray();
        char c = s2.charAt(4);
        int r = s2.indexOf('r');
```

## 1.2 Queue类型
- 队列用于将对象从一个程序传递给另外一个程序；
- 对于队列的操作基本上就是：新增插入，已有删除，元素查找几个操作。

### 1.2.1 基本Queue类型
- 使用代码可以简单描述以上三个操作:
Python中一般使用list数据结构，方法append新增，pop(0)去除队首的元素；pop()去除队尾的元素。
```python
"""
使用Python的list模拟队列和堆栈操作
"""
def mok_queue_stack():
    que = []
    que.append(11)
    que.append(12)
    que.append(13)

    #模拟队列的FIFO操作
    print(que.pop(0)) # 弹出第一个元素:11

    #模拟栈的FILO操作
    print(que.pop()) # 弹出最后一个元素:13
```

- Java中使用Linked List结构来实现操作：
其中各方法根据是否抛异常，区别罗列如下：
 类别 | 方法 | 返回异常与否
---|---|---
插入|add(E e)|容量限制发生时，会抛异常
插入|offer(E e)|对于容量限制队列，返回false，而不会抛异常的
删除|remove()|队列为空时，会抛异常
删除|poll()|队列为空时，返回null，不会抛异常

此外，对于队列，不可以插入null值。

```java
    private void opNormalQueue() {
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(11);
        q.add(14);
        q.offer(12);
        q.offer(13);
        q.forEach(System.out::println);

        System.out.println("-------done------");
        //FIFO--提取并删除队列首节点
        q.remove();
        q.poll();
        q.forEach(System.out::println); //输出 12 13

        //FILO--移除最后的节点，模拟堆栈
        ((LinkedList<Integer>) q).removeLast(); //去除13

        q.forEach(System.out::println); //输出12
    }
```
- 其中add和remove，容量限制时会报异常；offer和poll不抛异常，返回false

### 1.2.2 Priority Queue类型
- 操作办法和普通队列类似，区别就在于增加了一个比较器，可以根据优先级排序，让高优先级的元素先出队；

- 示例的Python代码，使用push和pop操作，如下：
```python
"""
堆排序的操作方法-使用push插入的办法
"""
import heapq
def op_prior_queue():
    nums = [2,3,1,54,132,32]
    heap = []
    for num in nums:
        heapq.heappush(heap,num)
    print(heap[0]) #输出 1
    print([heapq.heappop(heap) for _ in range(len(nums))])
    #输出 [1, 2, 3, 32, 54, 132]
"""
堆排序的操作方法-使用heapify初始化方法
"""
def heapify_prior_queue():
    nums = [2,3,1,54,132,32]
    heapq.heapify(nums)
    print([heapq.heappop(nums) for _ in range(len(nums))])
    #输出 [1, 2, 3, 32, 54, 132]
"""
```

- 示例的java代码：
```java
    /**
     * 组建一个整形的优先级队列，默认按照最小堆排列
     */
    private void opPriorityQueue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.add(3);
        pq.add(1);
        pq.add(4);
        pq.offer(7);
        int size = pq.size();
        //输出 1 3 4 7
        for (int i = 0; i < size; i++) {
            System.out.println(pq.poll());
        }
    }

    /**
     * 传入自定义的对象给二叉堆，注意需要实现comparator接口，才可进行排序操作
     */
    private void opPriorityQueueComparator() {
        PriorityQueue<Student> pq = new PriorityQueue<Student>(new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                if(o1.getScore() == o2.getScore()){
                    return o1.getName().compareTo(o2.getName());
                }
                return o1.getScore() - o2.getScore();
            }
        });
        pq.offer(new Student("xiaoming",12));
        pq.offer(new Student("meimei",10));
        pq.offer(new Student("lily",13));
        pq.offer(new Student("tony",9));
        
        //输出 name: tony,score: 9
        System.out.println(pq.poll());
    }
```

### 1.2.3 双端队列 Deque类型
- 两端都可以进行push和pop操作，兼具队列和栈的操作特性
- Python基本示例代码：
```python
"""
双端队列的操作方法-可以使用left从左侧操作；双向都可以进行插入和删除操作
"""
import collections
def op_deque():
    dq = collections.deque()
    dq.appendleft(1)
    dq.append(2)
    dq.append(3)
    print(dq.pop()) #输出3
    print(dq.popleft()) #输出1
```

- Java ArrayDeque操作示例代码：
```java
    private void opDeque() {
        Deque<Integer> deq = new ArrayDeque<>();

        //INSERT--新增操作清单
        deq.add(1); //Deque基类的方法，对于ArrayDeque类实际调用的addLast方法
        deq.addFirst(3); //队首插入，会抛容量异常
        deq.addLast(2); //队位插入，会抛容量异常

        deq.offer(6); //实际调offerLast
        deq.offerFirst(4); //类似addFirst，但是不抛容量异常
        deq.offerLast(5); //类似addLast，但是不抛容量异常
        deq.offerLast(7);
        deq.offerLast(8);

        //输出： 4,3,1,2,6,5,7,8
        System.out.println(StringUtils.join(deq.toArray(),","));

        //DELETE--删除操作清单
        deq.remove(); //Deque基类的方法，对于ArrayDeque类实际调用的removeFirst方法
        deq.removeFirst(); //队首出队，会抛队列为空异常
        deq.removeLast(); //队尾出队，会抛队列为空异常

        deq.poll(); //实际调pollFirst
        deq.pollFirst();
        deq.pollLast();

        //GET--获取操作清单 还剩: 6,5
        System.out.println(deq.getFirst()); //获取，但不移除首元素，会抛队列为空异常
        System.out.println(deq.getLast());

        deq.peek(); //实际调peekFirst，不会抛队列为空异常
        deq.peekFirst();
        deq.peekLast();
    }
```

## 1.3 Heap类型
- 分为最大堆和最小堆；主要操作也是包括新增数据和删除数据；
- 具体实现上为了维持堆的稳定，需要实现新增时写入树末位，然后逐步和父节点对比，上移到合适位置；
- 删除节点时，拿末位节点替换堆顶点节点，然后再新堆顶，和下节点比较，下移操作，以达到合适位置；
- 初始化建堆时，选择最后一个父节点，逐次进行下移动作，最后达到顶点，完成最大堆的构建。
* [一个外部的参考介绍](https://www.jianshu.com/p/6d3a12fe2d04)
- Java版本的实现代码举例：
```java
/**
 * 使用数组实现最大堆
 */
public class HeapWithArray {
    private void buildHeap(int[] arr){
        for (int i = arr.length / 2 - 1; i >=0 ; i--) {
            downShift(i, arr);
        }
    }

    /**
     * 删除首节点，让末位节点做头节点后，为了满足最大堆特性，需要在链路上下移操作
     * 或者在初始化堆时，父节点循环下移操作
     * @param index
     * @param arr
     */
    private void downShift(int index, int[] arr) {
        //记录好本节点和子节点index
        int parentIndex = index;
        int childIndex = 2 * parentIndex + 1;
        //备份下移节点值
        int temp = arr[parentIndex];
        while(childIndex <= arr.length - 1){
            //获取右节点，从而对比得出最大值子节点
            int rightIndex = childIndex + 1;
            if (rightIndex <= arr.length - 1 && arr[rightIndex] > arr[childIndex]) {
                childIndex = rightIndex;
            }
            //子节点和下移节点值对比，找到正确的地点即可
            if(temp >= arr[childIndex]){
                break;
            }
            //对父子index，进行下移操作
            arr[parentIndex] = arr[childIndex];
            parentIndex = childIndex;
            childIndex = childIndex * 2 + 1;
        }
        //最后的有效childIndex赋值给了parentIndex，被赋值为temp即可
        arr[parentIndex] = temp;
    }

    /**
     * 新增子节点上移操作
     * 主要逻辑是和上级父节点对比，新增节点值小于当前父节点时，停止上移
     * @param arr
     */
    private void upShift(int[] arr) {
        //获取新增节点和父节点的index
        int childIndex = arr.length - 1;
        int parentIndex = (childIndex - 1) / 2;
        //备份记录新增节点值
        int temp = arr[childIndex];
        //循环上移，和上级父节点比较
        while(childIndex > 0 && temp > arr[parentIndex]){
            //父子节点交换，相当于新增节点在上移
            arr[childIndex] = arr[parentIndex];
            childIndex = parentIndex;
            parentIndex = (parentIndex - 1) / 2;
        }
        //最后有效的parentIndex给了childIndex，选好位置，被赋值为备份的temp
        arr[childIndex] = temp;
    }

    public static void main(String[] args) {
        HeapWithArray ha = new HeapWithArray();
        int[] arr = {3,5,6,7,10,1,8,9,14};
        ha.buildHeap(arr);
        System.out.println(Arrays.toString(arr));


        //新增一个节点20 在最后
        int[] arr2 = new int[arr.length + 1];
        System.arraycopy(arr,0,arr2,0,arr.length);
        arr2[arr.length] = 20;
        ha.upShift(arr2);
        System.out.println(Arrays.toString(arr2));

        //删除首节点--直接使用尾元素赋值首元素
        int last = arr[arr.length - 1];
        int[] arr3 = Arrays.copyOf(arr,arr.length - 1);
        arr3[0] = last;
        ha.downShift(0, arr3);
        System.out.println(Arrays.toString(arr3));
    }
}
```
