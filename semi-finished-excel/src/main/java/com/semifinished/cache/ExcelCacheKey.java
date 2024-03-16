package com.semifinished.cache;

import lombok.Getter;

@Getter
public enum ExcelCacheKey {

    EXCEL("semi:excel:mapping", "Map<String, ObjectNode>");

    private final String key;
    private final String type;

    ExcelCacheKey(String key, String type) {
        this.key = key;
        this.type = type;
    }
}
