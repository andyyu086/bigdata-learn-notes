## Rabbit MQ的使用记录


### 1. 安装
安装RabbitMQ 参考链接：
https://blog.csdn.net/chao821/article/details/81744609

#### 1.1 安装Erlang
> yum install erlang

#### 1.2 安装Rabbitmq-server
> yum install  rabbitmq-server

#### 1.3 远程控制台和创建用户配置
https://www.jianshu.com/p/9c3d6d63931a

#### 1.4 RabbitMQ使用和原理
https://www.cnblogs.com/haixiang/p/10826710.html

### 2. Exchange类型
RabbitMQ 中的 Exchange 有多种类型，类型不同，Message 的分发机制不同，如下：
- Fanout：广播模式。这种类型的 Exchange 会将 Message 分发到绑定到该 Exchange 的**所有**Queue。
- Direct：这种类型的 Exchange 会根据 Routing key **精确**匹配，将Message分发到指定的Queue。
- Topic：这种类型的 Exchange 会根据 Routing key **模糊**匹配，将Message分发到指定的Queue。
- Headers: 和主题交换机有点相似，但是不同于主题交换机的路由是基于路由键，头交换机的路由值基于消息的header数据。主题交换机路由键只有是字符串,而头交换机可以是整型和哈希值.

### 3. 消息确认机制
- **生产者端** 消息的确认
是指生产者投递消息后，如果Broker收到消息，则会给我们生产者一个应答。
- **Broker端** 异常消息Return机制
Return Listener用于处理一些不可路由的消息。
正常情况下，消息生产者通过指定一个Exchange和Routing Key，把消息送达到某一个队列中去，然后我们的消费者监听队列，进行消费处理操作。
但是，在某些情况下，如果我们在发送消息的时候，当前的exchange不存在或者指定的路由key路由不到，这个时候如果我们需要监听这种不可达的消息，就要使用ReturnListener；
在基础API中有一个关键的配置项: Mandatory；如果为true，则监听器会接收到路由不可达的消息，然后进行后续处理，如果为false，那么broker端自动删除该消息。
- **消费者端** ACK机制
消费端进行消费的时候，如果由于业务异常我们可以进行日志的记录，然后进行补偿；如果由于服务器宕机等严重问题，那我们就需要手工进行ACK保障消费端消费成功。

### 4. Spring Boot内的使用
#### 4.1 Fanout模式
- Fanout模式会分发到exchange对应的所有queue;

- 1. Queue Exchange Binding三个的config定义Bean的代码如下：
```java
package com.patsnap.spt.smt.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {
    public static final String FANOUT_QUEUE_A = "dev.fanout.queue.a";
    public static final String FANOUT_QUEUE_B = "dev.fanout.queue.b";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String FANOUT_ROUTE_KEY = "ab.ee";

    //Queue定义
    @Bean
    public Queue fanOutAQueue() {
        return new Queue(FANOUT_QUEUE_A);
    }

    @Bean
    public Queue fanOutBQueue() {
        return new Queue(FANOUT_QUEUE_B);
    }

    //Exchange定义
    @Bean
    public FanoutExchange fanOutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    //Binding定义
    @Bean
    public Binding fanOutABinding(Queue fanOutAQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanOutAQueue).to(fanoutExchange);
    }

    @Bean
    public Binding fanOutBBinding(Queue fanOutBQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanOutBQueue).to(fanoutExchange);
    }
}
```
- 其中使用binding，把相应的queue绑定到exchange，在这种模式下route key不需要配置，实际可以随便输入的；

- 2. 发送者的示例代码如下：
```java
package com.patsnap.spt.smt.scenes.mq.fanout;

import com.patsnap.spt.smt.config.RabbitConfig;
import com.patsnap.spt.smt.po.Book;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FanoutSender {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(Book book) {
        System.out.println("fanout sent message: "+book);
        this.rabbitTemplate.convertAndSend(RabbitConfig.FANOUT_EXCHANGE,RabbitConfig.FANOUT_ROUTE_KEY,book);
    }
}
```
- 主要是使用AmqpTemplate的convertAndSend方法建object使用任意的route key发送到对应的exchange即可；

- 3. 消费者的示例代码如下：
```java
package com.patsnap.spt.smt.scenes.mq.fanout;

import com.patsnap.spt.smt.config.RabbitConfig;
import com.patsnap.spt.smt.po.Book;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = RabbitConfig.FANOUT_QUEUE_A)
public class FanoutReceiverA {

    @RabbitHandler
    public void process(Book book) throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("fanout A: receive message: "+book);
    }
}
```
- 主要使用@RabbitListener配置监听的队列FANOUT_QUEUE_A，和@RabbitHandler消费对应的消息；
- 当然，另外一个消费者类似代码可以配置消费FANOUT_QUEUE_B

- 4. 最后的controller层，触发消息发送的示例代码：
```java
@RestController
@RequestMapping(value = "/books")
@Api(tags = "MQ消息模块")
public class MQBookController {
    @Autowired
    private FanoutSender fanoutSender;

    @GetMapping(value = "/fanout")
    @ApiOperation(value = "fanout模式", notes = "发送fanout模式消息")
    public String fanoutMessage() {
        Book book = new Book();
        book.setId("1");
        book.setName("Study Spring Boot");
        this.fanoutSender.send(book);
        return "fanout success";
    }
}
```

#### 4.2 Topic 模式
- 具体使用代码架构和Fanout类似的，主要新增需要使用route key内配置；
- 使用同一个topicExchange建不同的queue配置到不同的route key；此外route key支持正则表达式匹配；

#### 4.3 Delay 模式
- 主要思路是先发送到TTL队列，再TTL队列配置超时死信队列的exchange和route key；这样消息就会延迟TTL时间后传入真正这行的process队列；
然后消费者直接消费process队列即可。
- 发送者的样例代码：
```java
@Component
public class DelaySender {
    private static final Logger log = LoggerFactory.getLogger(DelaySender.class);

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(Book book) {
        log.info("delay sent message: "+book);
        book.setName(book.getName()+" for delay");
        this.rabbitTemplate.convertAndSend(RabbitConfig.DELAY_TTL_EXCHANGE,RabbitConfig.DELAY_ROUTE_KEY_TTL,
                book,message -> {
                    message.getMessageProperties().setExpiration(5*1000+"");
                    return message;
                });
        log.info("send time is:"+LocalDateTime.now());
    }
}
```
- 注意上面发送到的是TTL对应的exchange和route key；而且指定了MessagePostProcessor参数，设置超时为5s；
- TTL队列的死信队列配置信息代码：
```java
    @Bean
    public Queue delayTtlQueue() {
        Map<String, Object> paras = new HashMap<>();
        paras.put("x-dead-letter-exchange",DELAY_PRO_EXCHANGE);
        paras.put("x-dead-letter-routing-key",DELAY_ROUTE_KEY_PRO);
        return new Queue(DELAY_QUEUE_TTL,true,false,false,paras);
    }
```
- 注意上面的死信队列dead-letter信息都是配置到Process的；这样消费者直接消费process队列即可，先消费正常队列一样。

