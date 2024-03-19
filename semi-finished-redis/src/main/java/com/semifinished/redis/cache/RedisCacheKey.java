package com.semifinished.redis.cache;

import lombok.Getter;

@Getter
public enum RedisCacheKey {
    MUTEX("semi:mutex:");

    private final String key;

    RedisCacheKey(String key) {
        this.key = key;
    }
}
