package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.jdbc.SqlDefinition;

/**
 * 参数校验接口
 */
public interface Validator {

    /**
     * 参数校验
     *
     * @param field         参数字段
     * @param value         参数内容
     * @param pattern       校验规则
     * @param msg           错误的提示信息
     * @param sqlDefinition SQL定义信息
     * @return 是否使用了该规则
     */
    boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition);
}
