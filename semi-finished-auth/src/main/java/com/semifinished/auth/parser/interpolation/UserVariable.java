package com.semifinished.auth.parser.interpolation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.interpolation.Interpolation;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserVariable implements Interpolation {

    @Override
    public boolean match(String key, JsonNode interpolatedKey) {
        return RequestUtils.getRequestAttributes(interpolatedKey.asText()) != null;
    }

    @Override
    public JsonNode value(String table, String key, JsonNode interpolatedKey, SqlDefinition sqlDefinition) {

        String value = RequestUtils.getRequestAttributes(interpolatedKey.asText());

        return TextNode.valueOf(value);
    }
}
