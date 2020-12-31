
## 对于推荐算法，简单的直接使用ALS，最小二乘法，进行用户和商品的稀疏矩阵的降维操作
1. 核心训练代码如下:

```python
    // 特征向量的个数
    val rank = 50
    // 正则因子 可以调整尝试多次
    val lambda = 0.01
    // 迭代次数 可以调整尝试多次
    val iteration = 10

    //进行训练，其中ratingRDD包含用户id，商品id和Rating打分
    val model = ALS.train(ratingRDD, rank, iteration, lambda)

    //training2为待预测的数据集，返回对应的rating值
    val predict = model.predict(training2).map {
      // 根据(userid, movieid)预测出相对应的rating
      case Rating(userid, movieid, rating) => ((userid, movieid), rating)
    }

    // 根据(userid, movieid)为key，将提供的rating与预测的rating进行比较，其中test2为已知的rating结果
    val predictAndFact = predict.join(test2)
    // 计算RMSE(均方根误差)
    val MSE = predictAndFact.map {
      case ((user, product), (r1, r2)) =>
        val err = r1 - r2
        err * err
    }.mean()
    val RMSE = math.sqrt(MSE)
    bestRMSE = RMSE
    model.save(sc, s"/tmp/BestModel/$RMSE")
    //可以调整参数多轮，得出最小RMSE，就是最优参数模型
```

2. 核心预测代码
```python
    //传入模型路径
    val model = MatrixFactorizationModel.load(sc, modelpath)
    //传入要预测的uid，获取5个推荐商品
    val rec = model.recommendProducts(uid, 5)
    //获取商品id，根据商品id，获取商品名称等
    val recmoviesid = rec.map(_.product)
```





