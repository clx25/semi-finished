package com.semifinished.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 聚合函数
 */
@Data
@AllArgsConstructor
public class AggregationFun {
    /**
     * 表名
     */
    private String table;
    /**
     * 字段名
     */
    private String column;
    /**
     * 函数规则
     */
    private String funPattern;
    /**
     * 别名
     */
    private String alias;
}
