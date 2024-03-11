package com.semifinished.auth.cache;

import lombok.Getter;

@Getter
public enum AuthCacheKey {

    USER("semi:auth:user", "Map<String, ObjectNode>"),
    SKIP_AUTH("semi:auth:skip_auth", "Map<String, String>"),
    CAPTCHA("semi:auth:captcha", "Map<String, String>");

    private final String key;

    AuthCacheKey(String key, String type) {
        this.key = key;
    }
}
