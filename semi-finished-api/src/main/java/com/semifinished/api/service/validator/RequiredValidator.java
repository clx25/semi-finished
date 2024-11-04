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
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"required".equals(pattern)) {
            return false;
        }
        if (value instanceof ValueNode) {
            Assert.isFalse(StringUtils.hasText(value.asText()), () -> new ParamsException(msg));
            return true;
        } else if (value instanceof ArrayNode) {
            Assert.isEmpty((ArrayNode) value, () -> new ParamsException(msg));
            return true;
        } else if (value instanceof ObjectNode) {
            Assert.isTrue(value.isEmpty(), () -> new ParamsException(msg));
            return true;
        }
        return false;
    }
}
