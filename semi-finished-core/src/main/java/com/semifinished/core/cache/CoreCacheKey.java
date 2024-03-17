package com.semifinished.core.cache;

import lombok.Getter;

/**
 * 缓存数据的key
 */
@Getter
public enum CoreCacheKey {
    /**
     * 数据库表字段名
     */
    COLUMNS("semi:core:columns:", "List<Column>"),
//    /**
//     * 排除字段
//     */
//    EXCLUDE_COLUMNS("semi:core:exclude_columns"),
//    /**
//     * 表名映射
//     */
//    TABLE_MAPPING("semi:core:actual_table"),
//    /**
//     * 字段名映射
//     */
//    COLUMN_MAPPING("semi:core:column_mapping"),
//    /**
//     * 默认数据源
//     */
//    DEFAULT_DATASOURCE("semi:core:default_datasource"),
    /**
     * json配置
     */
    JSON_CONFIGS("semi:core:json_configs", "Map<String, Map<String, ObjectNode>>");


    private final String key;

    CoreCacheKey(String key, String type) {
        this.key = key;
    }
}
