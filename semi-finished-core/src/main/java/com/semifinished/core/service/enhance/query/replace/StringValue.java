package com.semifinished.core.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.StringJoiner;

/**
 * 把集合内容转为字符串
 */
@Component
public class StringValue implements ValueReplacer {
    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        if (!"str".equals(pattern) || value instanceof ValueNode || value instanceof ObjectNode) {
            return value;
        }
        if (value == null) {
            return TextNode.valueOf("");
        }
        StringJoiner sj = new StringJoiner(",");
        for (JsonNode jsonNode : value) {
            String text = jsonNode.asText();
            if (StringUtils.hasText(text)) {
                sj.add(text);
            }
        }

        return TextNode.valueOf(sj.toString());
    }
}
