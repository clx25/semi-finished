package com.semifinished.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.jdbc.SqlDefinition;

public interface ValueReplacer {

    /**
     * 替换原始的值
     *
     * @param sqlDefinition SQL定义信息
     * @param pattern       替换规则
     * @param value         返回数据的值
     * @return 用这个返回的值替换原始的值
     */
    JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value);


}
