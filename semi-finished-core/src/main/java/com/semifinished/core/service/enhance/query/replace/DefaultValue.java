package com.semifinished.core.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;

@Component
public class DefaultValue implements ValueReplacer {
    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!pattern.startsWith("def")) {
            return value;
        }
        return TextNode.valueOf(value.isNull() ? pattern.substring(3) : value.asText());
    }
}
