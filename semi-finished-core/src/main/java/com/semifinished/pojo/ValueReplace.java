package com.semifinished.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 数据替换信息
 */
@Data
@AllArgsConstructor
public class ValueReplace {
    /**
     * 替换对应数据表名
     */
    private String table;
    /**
     * 替换对应数据字段名
     */
    private String column;
    /**
     * 替换规则
     */
    private String pattern;
}
