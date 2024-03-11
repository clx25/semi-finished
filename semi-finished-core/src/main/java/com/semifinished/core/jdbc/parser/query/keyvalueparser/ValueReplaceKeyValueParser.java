package com.semifinished.core.jdbc.parser.query.keyvalueparser;


import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ValueReplaceKeyValueParser implements KeyValueParamsParser {


    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!key.startsWith("#")) {
            return false;
        }

        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.JOIN, ParserStatus.DICTIONARY, ParserStatus.SUB_TABLE), () -> new ParamsException("内容替换规则位置错误"));

        String columns = value.asText(null);
        Assert.isFalse(StringUtils.hasText(columns), () -> new ParamsException("内容替换规则错误：" + key));

        sqlDefinition.addReplace(table, columns, key.substring(1));

        return true;
    }

    @Override
    public int getOrder() {
        return -1200;
    }
}
