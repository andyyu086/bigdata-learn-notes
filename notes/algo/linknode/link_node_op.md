# 1. 线性类型
## 1.1 线性表
类别 | 存储结构 | 随机访问复杂度 | 更新和删除复杂度
--- | --- | --- | ---
顺序表 | 顺序存储(扩容) | O(1) | O(n)
链表 | 链表存储(存储方便) | O(n) | O(1)

### 1.1.1 对一个链表实现逆序排序
1. 迭代的实现
```java
public Node reverseIteractive(Node head){
        Node preNode = null;
        while(head != null){
            //获取初始化next节点
            Node next = head.next;
            //本节点的next指向前节点
            head.next = preNode;

            //后移操作1--pre node移动为本节点
            preNode = head;
            //后移操作2--操作节点移动更新为next
            head = next;
        }

        return preNode;
    }
```
2. 递归的实现
```java
    public Node reverseRecursive(Node head){
        if(head.next == null){
            return head;
        }

        Node nextNode = head.next;
        Node last = reverseRecursive(nextNode);

        //第一个节点和第二个节点调换
        nextNode.next = head;
        //迭代内设为null之后迭代外会被上步的head赋值正确
        head.next = null;

        return last;
    }
```

3. 对链表部分倒序的实现
```java
    /**
     * 对链表，到第n个节点进行倒排
     */
    private Node successor = null;
    private Node reverseN(Node head,int n){
        if(n == 1){
            successor = head.next;
            return head;
        }

        Node nextNode = head.next;
        Node last = reverseN(nextNode, n - 1);

        //第一个节点和第二个节点调换
        nextNode.next = head;
        //循环中被外围的head覆盖，最后一次n为1后，恢复为下节点的next，即successor的有效赋值后
        head.next = successor;

        return last;
    }
```

4. 双向链表的倒序实现
```java
    /**
     * 以递归的方式反转双向链表
     * @param head
     * @return
     */
    private DNode reverse(DNode head){
        DNode curr = null;
        while(head != null){
            //赋值本节点
            curr = head;
            //本节点移动到下个节点
            head = curr.next;

            //本节点next指向前节点
            curr.next = curr.prev;
            //本节点的prev指向后节点(即head，已被赋值curr的next)
            curr.prev = head;
        }
        return curr;
    }
```

### 1.1.2 删除链表的第k个节点的实现
```java
    public Integer getLastK(Node head, int k){
        if(head == null){
            return null;
        }
        Node fast = head;
        Node slow = head;
        //fast先走k步
        while(--k > 0){
            fast = fast.next;
        }

        Integer lastKval = null;
        while(fast.next != null && slow.next != null){
            Node nextFastNode = fast.next;
            //|start----k----last|，其中fast先跑了k步，当fast到达末尾的时候,即slow到达倒数k节点的时候
            if(nextFastNode.next == null){
                //保留下last K的值
                lastKval = slow.next.value;
                //跳过即删除链表的last K节点
                Node next = slow.next.next;
                slow.setNext(next);
            }
            //fast slow通过next传递，迭代指针向后移动
            slow = slow.next;
            fast = nextFastNode;
        }
        return lastKval;
    }
```

