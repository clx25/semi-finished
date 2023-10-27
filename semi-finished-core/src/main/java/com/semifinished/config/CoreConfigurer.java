package com.semifinished.config;


import com.semifinished.pojo.Desensitization;

import java.util.List;
import java.util.Map;

/**
 * 使用代码的方式配置
 */
public interface CoreConfigurer {


    /**
     * 添加排除字段，该字段不会返回到前端
     */
    default void addExcludeColumn(Map<String, List<String>> excludeColumns) {
    }

    /**
     * 添加字段映射，用于前端隐藏实际字段名
     */
    default void addColumnMapping(Map<String, Map<String, String>> columnMapping) {
    }

    /**
     * 添加表名映射，用于前端隐藏实际表名
     */
    default void addTableMapping(Map<String, String> tableMappings) {

    }

    /**
     * 添加脱敏规则，如果添加了自定义脱敏器，那么就不需要设置left，right
     */
    default void addDesensitize(List<Desensitization> desensitize) {
    }


    /**
     * 返回默认数据库
     */
    default String chooseDb() {
        return null;
    }
}
