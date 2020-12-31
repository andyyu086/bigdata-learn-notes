## Spring Boot内 Redis的使用案例

### 1. 案例1 分布式ID获取方案
```java
import com.patsnap.spt.smt.service.DistIdGentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 使用RedisAtomicLong的incrementAndGet 生成分布式ID的方案
 */
@Service
public class DistIdGentorServiceImpl implements DistIdGentorService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public String generate(String key) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, stringRedisTemplate.getConnectionFactory());
        long num = redisAtomicLong.incrementAndGet();
        redisAtomicLong.expireAt(getTodayEndTime());
        String id = getTodayStr() + String.format("%08d",num);
        return id;
    }

    private String getTodayStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }

    private Date getTodayEndTime() {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY,23);
        instance.set(Calendar.MINUTE,59);
        instance.set(Calendar.SECOND,59);
        instance.set(Calendar.MILLISECOND,999);
        return instance.getTime();
    }
}
```

### 2. 案例2 分布式锁实现方案
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 使用redis的setIfAbsent操作，实现分布式锁的方案
 */
@Component
public class DistributedLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean getLock(String lockId,long exp){
        return redisTemplate.opsForValue()
                .setIfAbsent(lockId,"lock", exp, TimeUnit.SECONDS);
    }

    public boolean releaseLock(String lockId){
        return redisTemplate.delete(lockId);
    }
}
```

### 3. 数据库查询案例
- 使用Spring的相关注解，在操作数据库的时候，可以相应加入缓存的操作：
类 | 动作
org.springframework.cache.annotation.CacheEvict | 清除缓存
org.springframework.cache.annotation.CachePut | 生成缓存
org.springframework.cache.annotation.Cacheable | 没有的情况下，生成缓存；否则，读取缓存
- 缓存的配置示例代码, 加入EnableCaching注解，override Redis 可以的生成方式。
```java
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport{
	
	@Bean
	public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }
}
```
- TODO

### 5. HTTP session ID缓存案例
- POM 引入spring-session-data-redis
- 增加session的配置类
```java
package com.neo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400*30)
public class SessionConfig {
}
```