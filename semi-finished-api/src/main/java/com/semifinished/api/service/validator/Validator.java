package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.jdbc.SqlDefinition;

/**
 * 参数校验接口
 */
public interface Validator {

    /**
     * 参数校验
     * 返回的参数不表示是否通过校验，而是是否使用了该规则
     *
     * @param field         参数字段
     * @param value         参数内容
     * @param pattern       校验规则
     * @param msg           错误的提示信息
     * @param sqlDefinition SQL定义信息
     * @return 是否使用了该规则
     */
    boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition);
}
