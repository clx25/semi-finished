package com.semifinished.core.facotry;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.jdbc.SqlDefinition;

public interface SqlDefinitionFactory {

    /**
     * 获取SQL定义信息类
     *
     * @param params 请求参数
     * @return SQL定义信息类
     */
    SqlDefinition getSqlDefinition(JsonNode params);
}
