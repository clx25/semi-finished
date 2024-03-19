package com.semifinished.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认的缓存处理，使用Caffeine缓存数据
 */

public class CaffeineSemiCache implements SemiCache {
    private final Cache<String, Object> cache = Caffeine.newBuilder().build();

    @Override
    public <T> T getValue(String key) {
        return (T) cache.getIfPresent(key);
    }


    @Override
    public <T> T getHashValue(String key, String hashKey) {
        Map<String, T> hashValue = (Map<String, T>) cache.getIfPresent(key);
        if (hashValue == null) {
            return null;
        }
        return hashValue.get(hashKey);
    }

    @Override
    public <T> void addHashValue(String key, String hashKey, T value) {
        Map<String, T> hashValue = (Map<String, T>) cache.getIfPresent(key);
        if (hashValue == null) {
            hashValue = new HashMap<>();
        }
        hashValue.putIfAbsent(hashKey, value);
        cache.put(key, hashValue);
    }

    @Override
    public void initValue(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void initHashValue(String key, Map<String, ?> value) {
        cache.put(key, value);
    }


    @Override
    public void setValue(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void removeHashValue(String key, String hashKey) {
        Map<Object, Object> map = (Map<Object, Object>) cache.getIfPresent(key);
        if (map != null) {
            map.remove(hashKey);
        }
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
