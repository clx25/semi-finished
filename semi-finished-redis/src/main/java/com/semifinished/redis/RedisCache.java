package com.semifinished.redis;

import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ProjectRuntimeException;
import com.semifinished.core.utils.Assert;
import com.semifinished.redis.cache.RedisCacheKey;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class RedisCache implements SemiCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> T getValue(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public <T> T getHashValue(String key, String hashKey) {
        return redisTemplate.<String, T>opsForHash().get(key, hashKey);
    }

    @Override
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> void addHashValue(String key, String hashKey, T value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void initValue(String key, Object value) {
        execute(key, () -> redisTemplate.opsForValue().set(key, value));
    }

    @Override
    public void initHashValue(String key, Map<String, ?> value) {
        execute(key, () -> redisTemplate.opsForHash().putAll(key, value));
    }

    private void execute(String key, Executor executor) {
        //避免重复操作，设置5分钟的锁，且不解锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(RedisCacheKey.MUTEX.getKey() + key, 1, 5, TimeUnit.MINUTES);
        Assert.isTrue(locked == null, () -> new ProjectRuntimeException("redis获取锁异常"));
        if (locked) {
            redisTemplate.delete(key);
            executor.execute();
        }


    }

    interface Executor {
        void execute();
    }

    @Override
    public void removeValue(String key) {
        redisTemplate.delete(key);

    }

    @Override
    public void removeHashValue(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    @Override
    public <S> S getSource() {
        return (S) redisTemplate;
    }
}
