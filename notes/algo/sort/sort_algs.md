# 排序算法

## 冒泡排序
- 逐次对比相邻两个元素的大小，最大的值下沉；时间复杂度$$O(n^2)$$,空间复杂度$$O(1)$$
- Java实现举例代码：
```java
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = new int[]{4,2,8,9,5,7,6,3,10,1};
        arrSort(arr);
    }

    private static void arrSort(int[] arr) {
        //每个元素进行下沉
        for (int i = 0; i < arr.length - 1; i++) {
            //下面swap对比j-1的，所以初始化从1开始，到下沉完毕的i个元素为止
            for (int j = 1; j < arr.length - i; j++) {
                swap(arr,j-1,j);
            }
            System.out.println(Arrays.toString(arr));
        }
    }

    private static void swap(int[] arr, int i, int j) {
        if(arr[i] > arr[j]){
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
}
```
> 直接的结果如下：
[2, 4, 8, 5, 7, 6, 3, 9, 1, **10**]
[2, 4, 5, 7, 6, 3, 8, 1, **9**, 10]
[2, 4, 5, 6, 3, 7, 1, **8**, 9, 10]
[2, 4, 5, 3, 6, 1, **7**, 8, 9, 10]
[2, 4, 3, 5, 1, **6**, 7, 8, 9, 10]
[2, 3, 4, 1, **5**, 6, 7, 8, 9, 10]
[2, 3, 1, **4**, 5, 6, 7, 8, 9, 10]
[2, 1, **3**, 4, 5, 6, 7, 8, 9, 10]
[1, **2**, 3, 4, 5, 6, 7, 8, 9, 10]

## 选择排序
- 从前往后，依次选择最小值排列好，对比总次数为n^2 / 2; 交换次数最多n次。
- Java实现代码如下：
```java
public class SelectSort {
    public static void main(String[] args) {
        int[] arr = new int[]{4,2,8,9,5,7,6,3,1};
        selectSort(arr);
    }

    private static void selectSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int min = i;

            for (int j = i + 1; j < arr.length; j++) {
                //一共会对比 (n^2)/2 次
                if(arr[j] < arr[min]){
                    min = j;
                }
            }
            //交换i和下面的最新值,最多交换n次
            if(i != min){
                int temp = arr[i];
                arr[i] = arr[min];
                arr[min] = temp;
            }
            System.out.println(Arrays.toString(arr));
        }

    }
}
```

> 上例运行结果如下：
[**1**, 2, 8, 9, 5, 7, 6, 3, 4]
[1, **2**, 8, 9, 5, 7, 6, 3, 4]
[1, 2, **3**, 9, 5, 7, 6, 8, 4]
[1, 2, 3, **4**, 5, 7, 6, 8, 9]
[1, 2, 3, 4, **5**, 7, 6, 8, 9]
[1, 2, 3, 4, 5, **6**, 7, 8, 9]
[1, 2, 3, 4, 5, 6, **7**, 8, 9]
[1, 2, 3, 4, 5, 6, 7, **8**, 9]

## 插入排序
- 左侧维持有序子序列，每次后方的一个元素和前方有序序列比较，在正好比本数据小的元素后面位置插入数据；
- 最坏的情况需要N^2/2次比较和交换；平均情况下需要N^2/4次。
```java
public class InsertSort {
    public static void main(String[] args) {
        int[] arr = new int[]{4,2,8,9,5,7,6,1,3};
        insertSort(arr);
    }

    private static void insertSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int index = i;
            int iVal = arr[i];
            //如果前方元素值比新增值大，循环后移; 前节点小时，跳出
            while(index > 0 && arr[index - 1] > iVal){
                //元素后移
                arr[index] = arr[index - 1];
                index--;
            }
            //上方循环跳出时，本index就该赋值，相当于插值在此
            arr[index] = iVal;
            System.out.println(Arrays.toString(arr));
        }
    }
}
```

> 运行的结果为：(加粗标识了插入元素位置和index的位置)
[2, **4**|, 8, 9, 5, 7, 6, 1, 3]
[2, 4, **8**|, 9, 5, 7, 6, 1, 3]
[2, 4, 8, **9**|, 5, 7, 6, 1, 3]
[2, 4, **5**, 8, 9|, 7, 6, 1, 3]
[2, 4, 5, **7**, 8, 9|, 6, 1, 3]
[2, 4, 5, **6**, 7, 8, 9|, 1, 3]
[**1**, 2, 4, 5, 6, 7, 8, 9|, 3]
[1, 2, **3**, 4, 5, 6, 7, 8, 9|]

## 希尔排序
- 可变步长内，长距离先完成排序，然后缩短步长；从而对于较长的序列，相比插入排序可以更快完成排序。
- Java实现代码：
```java
public class ShellSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9,10,1,2,4,15,17,11,18,20,7,8,6,3};
        shellSort(arr);
    }

    private static void shellSort(int[] arr) {
        int length = arr.length;
        //设置初始步长为长度除以3
        int step = Math.round(length / 3);
        while(step > 0){
            //对于2段开始遍历比较
            for (int i = step; i < length; i++) {
                int j = i;
                //备份比较值
                int temp = arr[i];
                //和前面布段的相应值对比，将逆序的大值，进行后移操作
                while(j >= step && arr[j - step] > temp){
                    arr[j] = arr[j - step];
                    j = j - step;
                }
                //跳出时，即找到准确的位置
                arr[j] = temp;
            }
            //缩短步长，直到1步，完成排序
            step = Math.round(step / 2);
            System.out.println(Arrays.toString(arr));
        }
    }
}

```
> 运行的结果解释：
gap = 4,列方向排序：
*before*：
[9, 10, 1, 2, 
4, 15, 17, 11, 
18, 20, 7, 8, 
6, 3]
*after*：
[4, 3, 1, 2, 
6, 10, 7, 8, 
9, 15, 17, 11, 
18, 20]
---
> gap = 2，列方向排序：
*before*：
[4, 3, 
1, 2, 
6, 10, 
7, 8, 
9, 15, 
17, 11, 
18, 20]
*after*:
[1, 2, 
4, 3, 
6, 8, 
7, 10, 
9, 11, 
17, 15, 
18, 20]
---
> gap = 1,类似结果：
[1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 15, 17, 18, 20]

## 排序