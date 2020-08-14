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

### 3. 案例