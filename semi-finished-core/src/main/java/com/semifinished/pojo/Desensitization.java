package com.semifinished.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

/**
 * 脱敏字段配置实体类
 */
@Data
@Builder
public class Desensitization {
    /**
     * 表名
     */
    private String table;
    /**
     * 字段名
     */
    private String column;
    /**
     * 自定义脱敏器
     */
    private Function<String, String> desensitize;

    /**
     * 左侧保留长度
     */
    private double left;
    /**
     * 右侧保留长度
     */
    private double right;
}