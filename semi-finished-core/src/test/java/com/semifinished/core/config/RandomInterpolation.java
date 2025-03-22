package com.semifinished.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.interpolation.Interpolation;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomInterpolation implements Interpolation {
    @Override
    public boolean match(String key, JsonNode interpolatedKey) {
        return "random".equals(interpolatedKey.asText());
    }

    @Override
    public JsonNode value(String table, String key, JsonNode interpolatedKey, SqlDefinition sqlDefinition) {
        return IntNode.valueOf(new Random().nextInt(50) + 1);
    }
}
