package com.semifinished.core.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BooleanValue implements ValueReplacer {
    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!"boolean".equals(pattern)) {
            return value;
        }
        if (value == null) {
            return BooleanNode.getFalse();
        }
        if (value.isBoolean()) {
            return value;
        }
        if(value.isNumber()){
            return BooleanNode.valueOf(value.asBoolean());
        }
        String text = value.asText(null);
        if (!StringUtils.hasText(text)) {
            return BooleanNode.getFalse();
        }
        return "false".equalsIgnoreCase(text) ? BooleanNode.getFalse() : BooleanNode.getTrue();
    }
}
