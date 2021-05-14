


### 复杂度
- 时间复杂度；根据最高频次的行来计算，一般取高阶的表示；
- 平均时间复杂度，加权(概率)平均时间复杂度，平摊时间复杂度；
- 空间复杂度，一般是指array等空间使用量；

### 数组
- 空间连续的线性表，线性表是说单列前后元素关系的结构，空间连续是指内存存储地址是连续的；
- 由于地址连续，所有可以根据公式:
element addess = basic addres + data type size * index
快速计算出相应元素的地址，从而可以快速**随机根据索引查询**，复杂度O(1);
- 当然，副作用就是，由于要求地址连续，进行删除和插入操作，就得移动后面的元素，复杂度O(n);
- 对于更新的弊端，可以通过尾部新增和标记删除等手段来处理。

### ArrayList
- 相比较数组，封装了数组结构的扩容和拷贝数组功能；
- 不支持保存基础数据类型，只支持保存类；
- 如果底层上看，性能考虑的使用数组；应用层的话，便利开发的话，使用ArrayList;

### 链表
- 基于数组需要连续的内存；大数组的情况下，无法满足连续；可以使用链表，便于利于碎片的内存块；
- 为了可不连续，需要在数据项的后面加入一下链接信息，称为后续节点指针；链表查询复杂度O(n),插入和删除复杂度O(1)
- 对于链接节点的关系，链表分为3类： 单向链表，双向链表，和循环链表；
1. 双向链表，在插入和删除元素的时候，可以减少一次遍历；因为链表删除需要建前节点的next指针指到新的next节点，所以需要获取前节点；
而双向链表可以很快的获取，而单向链表需要再循环链表一遍。
2. 双向链表，对于有序链表的操作时，也可以省一半的时间，因为跟当前节点值比较，可以根据大小，切换查找的方向，两个方向都可以查找。
