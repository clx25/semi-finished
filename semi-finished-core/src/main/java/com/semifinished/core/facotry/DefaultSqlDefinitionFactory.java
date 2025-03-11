package com.semifinished.core.facotry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;

public class DefaultSqlDefinitionFactory implements SqlDefinitionFactory {
    @Override
    public SqlDefinition getSqlDefinition(JsonNode params) {
        Assert.isTrue(params instanceof ObjectNode, () -> new ParamsException("请求参数类型错误"));
        Assert.isFalse(params.isEmpty(), () -> new ParamsException("参数不能为空"));
        return new SqlDefinition((ObjectNode) params);
    }
}
