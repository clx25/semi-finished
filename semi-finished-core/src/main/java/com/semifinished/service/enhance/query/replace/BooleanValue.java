package com.semifinished.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.semifinished.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BooleanValue implements ValueReplacer {
    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!"boolean".equals(pattern)) {
            return value;
        }
        if (value.isBoolean()) {
            return value;
        }
        if (value.isArray()) {
            return BooleanNode.valueOf(!value.isEmpty());
        }
        String text = value.asText(null);
        if (!StringUtils.hasText(text)) {
            return BooleanNode.getFalse();
        }
        return "false".equalsIgnoreCase(text) ? BooleanNode.getFalse() : BooleanNode.getTrue();
    }
}
