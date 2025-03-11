package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RequiredValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"required".equals(pattern) && !"require".equals(pattern)) {
            return false;
        }
        Assert.isFalse(value == null, () -> new ParamsException(msg));
        if (value instanceof ValueNode) {
            Assert.isTrue(StringUtils.hasText(value.asText()), () -> new ParamsException(msg));
            return true;
        } else if (value instanceof ArrayNode) {
            Assert.notEmpty((ArrayNode) value, () -> new ParamsException(msg));
            return true;
        } else if (value instanceof ObjectNode) {
            Assert.isFalse(value.isEmpty(), () -> new ParamsException(msg));
            return true;
        }
        return false;
    }
}
