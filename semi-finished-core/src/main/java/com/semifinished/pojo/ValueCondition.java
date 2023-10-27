package com.semifinished.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存查询的where条件和增改的字段值
 */
@Data
@Builder
@AllArgsConstructor
public class ValueCondition {
    /**
     * 连接符，and/or
     */
    private String combination;
    /**
     * 表名
     */
    private String table;
    /**
     * 字段名
     */
    private String column;
    /**
     * 条件，> < =...
     */
    private boolean conditionBoolean;
    private String condition;
    /**
     * 占位符
     */
    private String argName;
    /**
     * 字段的值
     */
    private Object value;

    /**
     * 括号
     */
    private List<ValueCondition> brackets;

    public void addBrackets(ValueCondition valueCondition) {
        if (brackets == null) {
            brackets = new ArrayList<>();
        }
        brackets.add(valueCondition);
    }

    public void addBracketsAll(List<ValueCondition> valueConditions) {
        if (brackets == null) {
            brackets = new ArrayList<>();
        }
        brackets.addAll(valueConditions);
    }
}
