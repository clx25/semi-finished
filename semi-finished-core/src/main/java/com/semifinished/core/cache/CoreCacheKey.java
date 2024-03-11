package com.semifinished.core.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.pojo.Column;
import lombok.Getter;

import java.util.List;
import java.util.Map;

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
     * 自定义api
     */
    CUSTOM_API("semi:core:custom_api", "Map<String, Map<String, ObjectNode>>");


    private final String key;

    CoreCacheKey(String key, String type) {
        this.key = key;
    }
}
