package com.semifinished.core.constant;

import lombok.Getter;

/**
 * 标识SqlDefinition所处解析中的状态
 * 解析器或者增强器会根据不同的状态执行不同的逻辑
 */
@Getter
public enum ParserStatus {
    NORMAL(1, "普通"),
    SUB_TABLE(2, "子查询"),
    BRACKET(3, "括号查询"),
    JOIN(4, "join查询"),
    DICTIONARY(5, "字典查询");

    private final int status;
    private final String description;

    ParserStatus(int status, String description) {
        this.status = status;
        this.description = description;
    }
}
