package com.semifinished.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 默认的缓存处理，使用Caffeine缓存数据
 */

public class CaffeineSemiCache implements SemiCache {
    private final Cache<Object, Object> cache = Caffeine.newBuilder().build();

    @Override
    public <T> T getValue(String key) {
        return (T) cache.getIfPresent(key);
    }


    @Override
    public <T> T getValue(String key, String hashKey) {
        Map<String, T> hashValue = (Map<String, T>) cache.getIfPresent(key);
        if (hashValue == null) {
            return null;
        }
        return hashValue.get(hashKey);
    }

    /**
     * 从缓存中获取key对应的值，如果数据为<code>null</code>，那么从supplier中获取
     * 并且会把supplier中获取的数据添加到缓存中
     *
     * @param key      缓存的键
     * @param supplier 当获取的数据为<code>null</code>时，从supplier中获取
     * @param <T>      数据类型
     * @return 缓存中key对应的值，或supplier中获取的数据
     */
    @Override
    public <T> T getValue(String key, Supplier<T> supplier) {
        Object value = cache.getIfPresent(key);
        if (value != null) {
            return (T) value;
        }
        T t = supplier.get();
        cache.put(key, t);
        return t;
    }


    @Override
    public void setValue(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void removeValue(String key) {
        cache.invalidate(key);
    }

    @Override
    public <S> S getSource() {
        return (S) cache;
    }


}
