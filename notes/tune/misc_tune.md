

## 分析算子

### 预聚合
- sum max min count等算子可以先在子数据集进行预聚合，然后根据预聚合的初步结果，再算出最终结果；
- distinct关键字的操作，**无法**进行预聚合，所以性能消耗很大。

#### HLL
1. 使用HyperLogLog来解决distinct数据量大，近似计算偏差较大的问题；
HyperLogLog 算法，Spark 通过 partition 分片执行 MapReduce 实现 HLL 算法的伪代码如下所示：
- Map （每个 partition）
    初始化 HLL 数据结构，称作 HLL sketch
    将每个输入添加到 sketch 中
    发送 sketch
- Reduce
    聚合所有 sketch 到一个 aggregate sketch 中
- Finalize
    计算 aggregate sketch 中的 distinct count 近似值
值得注意的是，HLL sketch 是可再聚合的：
在 reduce 过程合并之后的结果就是一个 HLL sketch。
如果我们可以将 sketch 序列化成数据，那么我们就可以在预聚合阶段将其持久化，在后续计算 distinct count 近似值时，就能获得上千倍的性能提升。

