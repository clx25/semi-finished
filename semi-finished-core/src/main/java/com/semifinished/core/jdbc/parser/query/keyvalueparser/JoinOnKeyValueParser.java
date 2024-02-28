package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.semifinished.core.jdbc.parser.SelectParamsParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
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

        Assert.isTrue("@on".equals(key), () -> new ParamsException("@on规则位置错误"));
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
