package com.semifinished.service.enhance.query.replace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
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
            return objectMapper.readTree(text);
        } catch (JsonProcessingException e) {
            throw new ParamsException("json规则执行失败", e);
        }
    }
}
