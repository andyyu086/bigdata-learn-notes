# Scala基础笔记汇总
## 1.程序结构
### 1.1 判断和控制结构
```
val a = if(6>0) 1 else -1
for(i <- 1 to 5 if i%2 == 0){
  println(i)
}

val r = for(i <- Array(1,2,3,4,5)  if i%2 == 0)
  yield {
    println(i)
    i
  }
println(r)
```
- for循环可以增加if判断增加过滤语义
- 利用yield可以将循环结构里面的结果，收集到列表容器返回。

## 2.数据结构
### 2.1 数组
```
val bds = Array("BigData","Hadoop","Spark")
println(bds(0))
println(bds(1))
```
- Array的apply方式构建数组，和Java使用中括号不同，scala直接使用括号传入index，获取相应元素

### 2.2 元组

```
val tuple = ("BigData",2015,45.0)
println(tuple._1)

val (t1,t2,t3) = tuple
println(t1)
println(t2)
println(t3)
```
- 直接使用括号构建元祖，使用tuple._1 这样的下标获取index，
- 此外，可以使用模式匹配将元祖拆分传递给多个变量；
- 该功能类似python元祖的实现；而在java中，对于方法返回多值的场景下，一般需要使用一个result类封装返回。

### 2.3 容器
- Interable层级下包含：Seq,Set,Map；这三者的区别在其元素的索引方式；
- 其中，Seq根据整数从0开始索引，Map根据键值索引，Set是没有索引的。

#### 2.3.1 Seq序列
- 序列区分为LinearSeq序列(List和Queue)和IndexedSeq序列(ArrayBuffer和Vector)
```
    var strList = List("BigData","Hadoop","Spark")
    val add = "xx"::strList
    for(s <- add){
      println(s)
    }
    
    val vec1 = Vector(1,2)
    println(vec1)
    val vec2 = 4 +: vec1
    println(vec2)
    val vec3 = vec1 :+ 5
    println(vec3)

    val mut = ListBuffer(10,20,30)
    mut.insert(2,22)
    println(mut)

    val rg = new Range(1,5,1)
    println(rg)
```
- List,Vector默认都是不可变的，直接使用apply构建；List基于链表，支持使用"::"操作符进行新增操作；
- Vector支持使用"+:"和":+"操作符进行左右新增操作
- ListBuffer为可变的，具体可以参考例子中的insert方法
- Range可以利用第三个参数，构建带步长的序列

#### 2.3.2 Set集合
- 直接使用"+="操作符操作的例子
```
    var se = Set("My","Set")
    println(se)
    se += "My"
    println(se)
    se += "Your"
    println(se)
```

#### 2.3.3 Map和Iterator的构建
```
    val ma = Map("x"->"y","a"->"b")
    println(ma)

    val iter = Iterator("z","x","c")
    while(iter.hasNext){
      println(iter.next())
    }
```

## 3.类
- 用一个例子说明
```
class Counter {
  private var privateValue = 0
  def value = privateValue
  def value_(newVal:Int):Unit = {
    if(newVal > 0){
      privateValue = newVal
    }
  }

  def increment(step : Int):Unit = {
    privateValue += step
  }

  def current():Int={
    value
  }

  def show: Unit = {
    val c = Nested("xx")
    println("included nested name is:",c.name)
  }

  case class  Nested(name: String)
}

object CounterTest{
  def main(args: Array[String]): Unit = {
    val myCounter = new Counter
    myCounter.value_(5)
    myCounter.increment(1)
    println(myCounter.current)
    println(myCounter.value)
    myCounter.show
  }
}
```
1. 使用def定义方法，如increment增加step值，current返回当前值；
2. 使用value和value_(带下划线),定义类似java的getter和setter方法，以保护私有变量；
3. 使用case class可以实现类内嵌套类，方法可以直接使用；
4. 类构建和方法调用时，对于无传参的情况下，可以省略后面的空括号。

## 4.对象
### 4.1 单例对象
- 使用object构建单例对象，可实现类似java的静态成员

```
class Person(val name: String) {
  def info(): Unit = {
    printf("the name is %s; id is:%d \n",name,id)
  }

  private val id = Person.newPersonId()
}

object Person {
  private var lastId = 0
  def newPersonId() = {
    lastId += 1
    lastId
  }

  def main(args: Array[String]): Unit = {
    val p1 = new Person("Lilei")
    val p2 = new Person("Hanmei")
    p1.info()
    p2.info()
  }
}
```
1. 和class同名的Person object，在同一个文件中，称为伴生对象
2. 在object中实现main后，运行的结果类似java中操作一下静态变量id，实现全局自增，结果如下：
> *the name is Lilei; id is:1  
the name is Hanmei; id is:2*

### 4.2 apply
- 直接再类内定义apply方法之后，即可使用apply方式构建对象

```
class TestApply {
  def apply(param: String){
    println("apply method param:"+param)
  }
}

object TestApplyTest {
  def main(args: Array[String]): Unit = {
    val myObj = new TestApply
    myObj("test1")
    myObj.apply("test2")
  }
}
```
- 或者在伴生对象中定义apply方法，具体构建时，也是会直接调用的

```
class Car(name: String) {
  def info(){
    println("car name is:"+name)
  }
}

object Car {
  def apply(name: String): Car = new Car(name)
}

object CarApplyTest {
  def main(args: Array[String]): Unit = {
    val myObj = Car("TWT")
    myObj.info()
  }
}
```

### 4.3 继承
#### 4.3.1 抽象类
- 抽象类的定义例子
```
abstract class AbsCar(val name: String) {
  val carBrand:String
  var age:Int = 0
  def info()
  def greeting(): Unit ={
    println("welcome to my car!")
  }
  def this(name: String,age:Int){
    this(name)
    this.age = age
  }
}
```
1. 只要类中有未赋值的变量(如carBrand)或者未定义的方法(如info)，class前就得加abstract关键字，定义为抽象类
2. 具体的抽象方法和字段不需要加abstract关键字
3. scala内抽象类无法实例化，必须被子类继承才可以被实例化后使用

#### 4.3.2 类的继承

```
class BMCar(override val name:String) extends AbsCar(name) {
  override val carBrand: String = "BMW"

  override def info(): Unit = {
    printf("The brand is %s and age is %d \n",carBrand,age)
  }

  override def greeting(): Unit = {
    println("welcome to BMW car!")
  }
}

class BYCar(name:String, age:Int) extends AbsCar(name,age) {
  val carBrand: String = "BY"

  def info(): Unit = {
    printf("The brand is %s and age is %d \n",carBrand,age)
  }

  override def greeting(): Unit = {
    println("welcome to BY car!")
  }
}

object MyCarTest {
  def main(args: Array[String]): Unit = {
    val car1 = new BMCar("Bob ")
    val car2 = new BYCar("Boy ",11)
    show(car1)
    show(car2)
  }

  def show(theCar: AbsCar) = {
    theCar.greeting()
    theCar.info()
  }
}
```
1. 子类的构造函数和传参和父类的主构造函数相同的情况下，传参需要加入override；(override val name)
2. 在买了函数内，定义show方法定义父类Abschar，传入不同的子类对象，从而实现多态

#### 4.3.3 Scala基础类的层级

```
graph LR
Any-->AnyVal
Any-->AnyRef

AnyVal-->Char/Byte
AnyVal-->Short/Int/Long/Float/Double
AnyVal-->Boolean
AnyVal-->Unit
Char/Byte-->Nothing,下同

AnyRef-->Scala的引用
AnyRef-->Java的String
AnyRef-->其他Java的引用
Scala的引用-->Null,下同

Null,下同-->Nothing

```
- 所有类型最下级为Nothing,包括Null的下级
- 所有引用下级为Null

#### 4.3.4 Option和模式匹配
- Option的层级结构，主要用于避免NPE异常
```
graph LR
Option-->Some
Option-->None
```
- 一个组合的例子
```
class OptionTest {
  case class Book(name:String,price:Double)
  val books = Map("hadoop" -> Book("hadoop",25.5))

  def matchTest(){
    for (ele <- List(6,9.5,"Spark","xx")){
      val ste = ele match {
        case i:Int => i+" is int value"
        case d:Double => d+" is double value"
        case "spark" => "spark is found."
        case s:String => s+" is string value"
        case _ => ele + " is unexpected value"
      }
      println(ste)
    }

    for (book <- books.values){
      book match {
        case Book("hadoop",25.5) => println("Hello,hadoop")
        case _ => println("Unexpect book: "+book)
      }
    }
  }
}

object OptionTest {
  def main(args: Array[String]): Unit = {
    val ot = new OptionTest
    val bk = ot.books;

    println(bk.get("hadoop"))
    println(bk.get("hive"))

    println(bk.get("hadoop").get)
    println(bk.get("hive").getOrElse(("Unknown",0)))
    ot.matchTest()
  }
}
```
> *Some(Book(hadoop,25.5))  
None  
Book(hadoop,25.5)  
(Unknown,0)  
6 is int value  
9.5 is double value  
Spark is string value  
xx is string value  
Hello,hadoop*  

1. Map中get获取的Some对象，然后再次get获取实际的Book对象
2. Map获取不到value的情况下，返回None，或者通过getOrElse返回default值
3. match的case可以支持多类型判断，直接判断类型或者具体值；
4. 此外match支持case class的对象匹配

## 5.函数式编程
### 5.1 基本举例
- 总体上把函数看成和变量类似，尽量传入val的不可变参数，从而可以实现并发

一个例子：
```
    val counter = (value:Int) => value +1;
    println(counter(5))
    
    val add = (a:Int,b:Int) => a + b
    println(add(3,5))
    
    val show = (s:String) => println(s)
    show("hello world!")
```
1. 使用lambda匿名函数表达式，"=>"左侧为传参定义，右侧为实际运行表达式，函数的返回值类型会根据表达式的返回值类型自动推算得到；
2. 定义完成函数后，直接传参调用即可；多个参数用法相同；具体运行表达式可以引用其他方法，比如例子中的println

### 5.2 简化举例
```
    val cunt = (_:Int) + 1
    println(cunt(8))

    val add2 = (_:Int) + (_:Int)
    println(add2(8,2))

    val li = List(1,2,3)
    val m2 = li.map(_*2)
    println(m2)
```
1. 当传参只在计算表达式中出现过一次，就可以直接用下划线(_)代替传参和实际运行变量
2. 在写明类型的情况下，需要跟下划线一起加括号
3. List的map方法可以根据list内的参数类型，推断出传参类型，所以可以直接使用，不需要定义。

### 5.3 高阶函数
首先定义两个递归函数：
```
  def powerCount(x: Int):Int = {
    if(x == 0){
      1
    }else{
      2 * powerCount(x - 1)
    }
  }

  def sumFunc(f:Int => Int,a:Int,b:Int):Int = {
    if(a > b){
      0
    }else{
      f(a) + sumFunc(f,a+1,b)
    }
  }
```
- 其中，sumFunc函数的f参数为函数式参数；可以接受不同函数传入；

实际的调用举例代码：
```
    println(sumFunc(x => x,1,5))
    println(sumFunc(x => x*x,1,5))
    println(sumFunc(powerCount,1,5))
```
1. 上面的3个例子中，整体上是使用将a传入调用f计算后，  
自增a后，继续迭代调用f；循环累加操作，直到a达到b为止结束。
2. 3行代码，分别传入了x，x*x 以及powerCount 函数；简化操作，实现函数式编程

### 5.4 容器操作函数
### 5.4.1 foreach函数
```
    val list = List(1,2,3,4,5)
    val f = (i:Int) => println(i)
    list.foreach(f)

    val univs = Map("XMU" -> "XiaMen University","THU" -> "Tsinghua University")
    univs foreach(kv => println(kv._1+":"+kv._2))
    univs foreach{case(k,v) => println(k+":"+v)}
```
1. foreach接受函数f，可以直接遍历list
2. univs Map直接使用中缀表达式实现函数式表达，空格后foreach传入kv或者case调取lambda函数进行操作

### 5.4.2 map/filter/reduce函数
```
    val books = List("Hadoop","Hive","HDFS")
    books.map(s => s.toUpperCase).foreach(println)
    books.map(_.length).foreach(println)

    univs filter(kv => kv._2 contains "XiaMen") foreach println

    println(list.reduce(_ + _))
    println(list.sum)

    println(list.reduce(_ * _))
    println(list.product)

    val ret = list map(_.toString) reduce((x,y)=>s"f($x,$y)")
    println(ret)
```
1. 对于books List使用map实现映射操作，进行数据转化，最后使用foreach打印
2. 对univs使用中缀表达式调取filter进行数据过滤后，再次中缀foreach打印
3. 对list使用reduce算子进行累计sum，求积等操作
4. 最后使用reduce进行元组后拼接而成，结果为"f(f(f(f(1,2),3),4),5)"

### 5.4.3 partition/groupBy/grouped函数

```
    println(list.partition(_<3))
    println(list.groupBy(x => x % 3))
    println(list.grouped(3).next())
```
> *(List(1, 2),List(3, 4, 5))  
Map(2 -> List(2, 5), 1 -> List(1, 4), 0 -> List(3))  
List(1, 2, 3)*

1. partition根据传参函数返回值，返回值相同的落在同一个分区；例子中是根据返回true/false,确定两个分区；还是返回list
2. groupBy根据传参函数得到Map的返回值为key，value为对应的value的list
3. grouped每次切割3个元素，最后一个子list可能不足3个元素

