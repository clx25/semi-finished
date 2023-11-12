package com.semifinished.service.enhance.query.replace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonValue implements ValueReplace {
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!"json".equals(pattern)) {
            return value;
        }
        try {
            return objectMapper.readTree(value.asText(""));
        } catch (JsonProcessingException e) {
            throw new ParamsException("json规则执行失败", e);
        }
    }
}
