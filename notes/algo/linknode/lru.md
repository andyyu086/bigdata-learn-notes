
## LRU的几种实现

### 使用单向链表实现
- 插入head时，需要记录当前存在的个数；
- get操作时，存在节点即先删除，然后更新为新head；如果不存在即插入到head；如果超限即删除尾节点。
- 单向链表整体复杂度较高，最起码为O(N)
```java
public class LruNode {
    private Node head = new Node(-1);

    private int max_size = 10;
    private int exist_size = 0;

    private void showAll(){
        NodeUtils.revShow(head.next);
    }

    private void putHead(Node node){
        if(node != null){
            head.next = node;
            Node tmp = node;
            while (tmp != null){
                tmp = tmp.next;
                exist_size++;
            }
        }
    }

    private Node get(int findKey){
        Node cur = head;
        Node parent = null;
        while (cur != null){
            //找得到就将该节点移到首节点位置
            if(cur.value == findKey){
                //删除后，插入头部
                //删除本节点，即将本节点的子节点赋值给其父节点的next
                parent.next = cur.next;
                //首部插入，即源head next的首节点赋值给本节点的next
                cur.next = head.next;
                //head重新连接到新首：本节点。
                head.next = cur;

                return cur;
            }
            //备份父节点和向后移动游标cur节点
            if(cur.next != null){
                parent = cur;
            }
            cur = cur.next;
        }

        //没有找到,判断已存在的数据量
        Node add = new Node(findKey);
        add.next = head.next;
        head.next = add;
        exist_size++;

        //以上cur已经遍历到最后了
        if(exist_size > max_size){
            //删除最后的node
            parent.next = null;
        }

        return add;
    }

    public static void main(String[] args) {
        Node n = new Node(1);
        Node next = new Node(2);
        Node thrd = new Node(3);
        Node four = new Node(4);
        n.setNext(next);
        next.setNext(thrd);
        thrd.setNext(four);
        LruNode lruNode = new LruNode();
        lruNode.putHead(n);
        lruNode.showAll();
        lruNode.get(4);
        System.out.println("排序后---");
        lruNode.showAll();
        System.out.println("查新数据---");
        lruNode.get(5);
        lruNode.get(6);
        lruNode.get(7);
        lruNode.get(8);
        lruNode.get(9);
        lruNode.get(10);
        lruNode.showAll();
        System.out.println("溢出一个最老的数据---");
        lruNode.get(11);
        lruNode.showAll();
    }
}
```

2. 使用双向链表和HashMap实现
- 双向链表加head tail节点，可以方便的操作父节点和头节点，复杂度为O(1)
- HashMap存储key和具体节点，也是常量复杂度O(1)
```java
import java.util.HashMap;

public class LruDNode {
    private DNode head = null;
    private DNode tail = null;
    private int capacity;
    HashMap<Integer,DNode> map = new HashMap<>();

    public LruDNode(int capacity){
        this.capacity = capacity;
    }

    private int get(int findKey){
        if(map.containsKey(findKey)){
            DNode dNode = map.get(findKey);
            delete(dNode);
            setHead(dNode);
            return dNode.value;
        }

        return -1;
    }

    /**
     * 删除节点(链接关系)
     * @param node
     */
    private void delete(DNode node){
        //如果有前节点，让其next到后节点
        if(node.prev != null){
            node.prev.next = node.next;
        }else{
            //本节点就是最前，head到次节点
            head = node.next;
        }

        //后节点不为空，让后节点prev到本节点的前节点
        if(node.next != null){
            node.next.prev = node.prev;
        }else{
            //本节点为尾节点
            tail = node.prev;
        }
    }

    /**
     * 插入到头部
     * @param node
     */
    private void setHead(DNode node){
        //之前链接的head(实际指向的就是实际的首节点，可以认为head指针),移到新节点的next
        node.next = head;
        node.prev = null;

        //之前的首节点，prev指向新首节点node
        if(head != null){
            head.prev = node;
        }
        //head指针更新为新首node
        head = node;

        if(tail == null){
            tail = head;
        }
    }

    /**
     * 更新或插入数据
     * @param key
     * @param value
     * @return
     */
    private void set(int key,int value){
        if(map.containsKey(key)){
            DNode old = map.get(key);
            old.value = value;
            delete(old);
            setHead(old);
        }else{
            DNode newNode = new DNode(key,value);
            if(map.size() >= capacity){
                map.remove(tail.key);
                delete(tail);
                setHead(newNode);
            }else{
                setHead(newNode);
            }
            map.put(key,newNode);
        }
    }



    public static void main(String[] args) {
        LruDNode lruDNode = new LruDNode(10);
        lruDNode.set(1,100);
        lruDNode.set(10,99);
        lruDNode.set(11,98);
        lruDNode.set(12,96);
        lruDNode.set(13,95);
        lruDNode.set(14,94);
        lruDNode.set(15,93);
        lruDNode.set(16,92);
        lruDNode.set(17,91);
        lruDNode.set(18,90);
        lruDNode.set(20,89);

        System.out.println(lruDNode.get(1));
        System.out.println(lruDNode.get(10));
        System.out.println(lruDNode.get(15));

        System.out.println(lruDNode.get(18));
        System.out.println(lruDNode.get(20));
        System.out.println(lruDNode.get(12));
    }
}
```

3. 使用LinkedHashMap实现
- java中比较简单的实现方式，其中LinkedHashMap构造函数，支持传入accessOrder为true是根据访问顺序排序；
- 此外，复写removeEldestEntry方法即可以实现去除最老元素的逻辑。

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class LruLinkedMap {
    LinkedHashMap<Integer,Integer> map;

    public LruLinkedMap(int capacity){
        map = new LinkedHashMap<Integer, Integer>(capacity,0.75f,true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    private int get(int findKey){
        return map.getOrDefault(findKey,-1);
    }

    private void set(int key,int value){
        map.put(key,value);
    }



    public static void main(String[] args) {
        LruLinkedMap lruDNode = new LruLinkedMap(10);
        lruDNode.set(1,100);
        lruDNode.set(10,99);
        lruDNode.set(11,98);
        lruDNode.set(12,96);
        lruDNode.set(13,95);
        lruDNode.set(14,94);
        lruDNode.set(15,93);
        lruDNode.set(16,92);
        lruDNode.set(17,91);
        lruDNode.set(18,90);
        lruDNode.set(20,89);

        System.out.println(lruDNode.get(1));
        System.out.println(lruDNode.get(10));
        System.out.println(lruDNode.get(15));

        System.out.println(lruDNode.get(18));
        System.out.println(lruDNode.get(20));
        System.out.println(lruDNode.get(12));
    }
}
```