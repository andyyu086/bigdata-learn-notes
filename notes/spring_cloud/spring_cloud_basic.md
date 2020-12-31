## Spring Cloud 常识基础版

### 1. Eureka服务发现
- 使用注解EnableEurekaServer进行创建server端，yml配置文件配置好端口即可；
- 使用注解EnableDiscoveryClient创建客户端，yml主要配置端口和查看服务的网络网卡等。
- 使用两者yml配置中的service-url，即可实现client端的服务注册和发现。

### 2. Ribbon负载均衡
- eureka server端和上面类似不变；
- 可以启动两个客户端，比如一个订单服务，一个生产服务；订单服务会访问生产服务；
- 此外，生产服务可以不同节点或端口启动多个；然后在client端均衡地访问他们；
- 主要工作在订单服务的client端，对RestTemplate加LoadBalanced注解，即可获得负载均衡，默认是用轮询算法的；
- 具体在client端的controller层，直接通过server端的service id请求对应的api即可，框架会自动根据算法分配到相应生成服务的ip端口；
- 当然，对于client端的负载均衡算法，也可以自定义实现，主要使用RibbonClient注解指定相应实现IRule接口的算法，比如随机算法RandomRule。

### 3. Ribbon的Feign HTTP访问
- 主要是对上方第2点的补充，不再使用RestTemplate，而是使用FeignClient注解，类似service层，封装HTTP实现底层，在controller层直接调用service层对应接口即可。
- 此外Feign功能，自动带负载均衡功能。

### 4. Ribbon的Feign 拦截器
- 上步的基础上补充拦截器功能，
- 新增一个Configuration注解的FeignInterceptor拦截器，可以对RequestTemplate等进行拦截操作；

### 5. Hystrix 熔断
- 使用HystrixCommand注解定义对应api，通过指定fallbackMethod属性可以自动熔断后执行的default等异常报错逻辑方法。
- yml文件中配置management的endpoints暴露include: 'hystrix.stream'等配置。
- 此外对于FeignClient实现，同样通过fallback属性传入异常报错逻辑。

### 6. Gateway 网关
- 需要新增一client端，命令如：gateway-server，在yml配置文件中对gateway:routes，进行相应url和service id的定义；
- 具体的service id对应的client端服务业务逻辑不变，具体路由逻辑由gateway server来负责；
- 除了路由之外，还可以实现权限控制等复杂的逻辑。

### 7. Zuul 网关
- 主要用于鉴权，路由，服务迁移，限流，安全等；
- 对于zuul服务器，主要在application入口添加EnableZuulProxy注解；在yml文件添加zuul:routes:路由配置相应的url path到相应的server id;
- 使用主要访问zuul服务的端口，然后zuul服务会根据路由规则，路由到相应的服务端进行rest请求返回；
- 除了路由之外，还可以实现过滤功能，具体时间点可以在：路由前，路由时，路由后，以及发生错误调用时；
- 对于过滤功能，主要在zuul服务实现一个继承ZuulFilter的component即可，具体实现逻辑在run方法内实现，比如判断token的情况等：
```java
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info(String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString()));
        Object accessToken = request.getParameter("token");
        if(accessToken == null) {
            log.warn("token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().getWriter().write("token is empty");
            }catch (Exception e){}

            return null;
        }
        log.info("ok");
        return null;
    }
```

### 8. Config 配置服务器
- 将分布式系统中的配置使用Config server进行统一配置；各个实际服务器可以直接从配置服务器获取相应的配置；
- 在配置服务器端：yml配置文件内配置好存放配置文件的git仓库地址分支等；在application启动类增加EnableConfigServer注解即可；
- 在实际调用配置服务器端：在其yml配置文件加入spring.cloud.config.uri指定配置服务器的地址等信息即可。
- **高可用**：如果用实现配置服务器的高可用，也可以将Config server加入到Eureka注册集群，然后，可以启动多台配置服务器；
然后在获取端yml配置：spring.cloud.config.discovery.serviceId=config-server，注意直接指定在注册中心的server id即可；
而且还可以实现config server的负载均衡和高可用。

### 9. Bus 消息总线
- 使用消息总线来进行服务间通信，配置同步等；
- 对于配置同步，使用starter-bus-amqp和actuator，使用rabbitmq，post actuator/bus-refresh接口，触发client重新获取config server的新配置，而且多个client间都会在rabbitmq内通信保持配置同步；


### 10. Sleuth 服务链路追踪
- 如果使用zipkin来监控跟踪的话，首先搭建zipkin server，简单的可以下载相应的exec jar进行启动；
- 具体应用服务器端，pom引入starter-zipkin，然后再配置文件中zipkin server的url，比如：spring.zipkin.base-url=http://localhost:9411
- 如果我们启动多个应用服务器之间相互调用操作完成后，就可以在zipkin server，相应的9411端口，通过页面进行服务间的Find Traces操作，分析相关服务间调用耗时和调用顺序



## 进阶原理篇
### Feign
- https://www.fangzhipeng.com/springcloud/2017/08/11/sc-feign-raw.html

### Eureka
- https://www.fangzhipeng.com/springcloud/2017/08/11/eureka-resources.html

1. 该可用架构图
![](https://raw.githubusercontent.com/Netflix/eureka/master/images/eureka_architecture.png)
2. 注册进行的4个主要步骤：
a. Register(注册): 客户端将本身的ip，端口，serverid等信息发送给server端；
b. Renew(续约): 建立完链接之后，客户端先服务端发送心跳，默认每30秒获取一次；
c. Fetch(Get) Register: 获取配置信息，向服务器端获取所有注册表信息，一般同步压缩的JSON格式进行发送；
d. Cancel: 客户端进行下线处理，发送相关下线信息给服务器端，以及进行注册表信息移除；
e. Eviction: 服务剔除，默认发现90秒服务器端没有接收到客户端的心跳信息，会将该客户端剔除注册表；
3. 此外，改图server端进行了集群化操作，多机同步备份注册表信息，保证高可用；
各个application端，也可以区分为服务调用方和生产方；但是，在Eureka注册关系上，他们都是Eureka client；
4. Eureka保护模式，主要是指server端会判断如果短时间内很多client端断链，会启动保护模式，认为可能是自身网络问题导致的，启动后，不在进行client的超时剔除操作；以保护整体集群的稳定。

### Ribbon
- https://www.fangzhipeng.com/springcloud/2017/08/11/Ribbon-resources.html
- Ribbon的负载均衡，主要通过LoadBalancerClient来实现的，而LoadBalancerClient具体交给了ILoadBalancer来处理，
ILoadBalancer通过配置IRule、IPing等信息，并向EurekaClient获取注册列表的信息，并默认10秒一次向EurekaClient发送“ping”,
进而检查是否更新服务列表，最后，得到注册列表后，ILoadBalancer根据IRule的策略进行负载均衡。

### 各系统串联图
![](https://img-blog.csdnimg.cn/20181113100554824.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2ZvcmV6cA==,size_16,color_FFFFFF,t_70)


