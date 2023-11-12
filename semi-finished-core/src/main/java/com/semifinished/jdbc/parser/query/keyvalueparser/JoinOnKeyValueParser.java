package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.util.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * on规则不能单独存在
 */
@Component
@AllArgsConstructor
public class JoinOnKeyValueParser implements SelectParamsParser {
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        Assert.isTrue("@on".equals(key), () -> new ParamsException("@on规则需要配合join规则使用：" + value));
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
