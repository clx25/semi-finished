package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

/**
 * 判断boolean类型
 */
@Component
public class BooleanValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"boolean".equals(pattern)) {
            return false;
        }

        if (value == null) {
            return true;
        }
        Assert.isTrue(value instanceof BooleanNode, () -> new ParamsException(msg));

        Assert.isTrue(value.asBoolean(), () -> new ParamsException(msg));

        return true;
    }
}
