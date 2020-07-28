# 快慢指针的操作

## 1.获取链表倒数k个节点
```java
    /**
     * 获取列表的倒数k个节点
     * @param head
     * @return
     */
    Node findLastK(Node head, int k){
        Node fast,slow;
        fast = slow = head;
        //先让fast移动k步
        while(k-- > 0){
            fast = fast.next;
        }
        //当fast到达链表末端的时候，slow就正好到达倒数k(即，n-k节点)的位置
        while(fast != null){
            fast = fast.next;
            slow = slow.next;
        }
        return slow;
    }
```

## 2. 特殊场景，获取链表的中心节点问题
```java
    /**
     * 获取列表的中间节点
     * @param head
     * @return
     */
    Node findMiddle(Node head){
        Node fast,slow;
        fast = slow = head;
    
        while(fast != null){
            //fast 以slow两倍的速度向末端移动，当fast到达最后节点时，
            //总节点数为奇数时，slow在中间节点；为偶数时，slow在中心偏右节点。
            fast = fast.next.next;
            slow = slow.next;
        }
        return slow;
    }
```
## 3. 类似场景，判断是否有环的存在
```java
    /**
     * 判断链路是否有环
     * @param head
     * @return
     */
    boolean hasCycle(Node head){
        Node fast,slow;
        fast = slow = head;

        while(fast != null && fast.next != null){
            fast = fast.next.next;
            slow = slow.next;
            //如果fast可以追上slow，就说明有环存在
            if(fast == slow){
                return true;
            }
        }
        return false;
    }
```

