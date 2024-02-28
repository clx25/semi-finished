package com.semifinished.core.service.enhance.query.replace;

import com.semifinished.core.exception.ParamsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@AllArgsConstructor
public class JsonValue implements ValueReplacer {
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!"json".equals(pattern)) {
            return value;
        }

        String text = value.asText(null);
        if (!StringUtils.hasText(text)) {
            return value;
        }
        try {
            return objectMapper.readValue(text, ObjectNode.class);
        } catch (JsonProcessingException e) {
            throw new ParamsException("json规则执行失败", e);
        }
    }
}
