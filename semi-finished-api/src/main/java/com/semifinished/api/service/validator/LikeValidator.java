package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

@Component
public class LikeValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        boolean left = pattern.startsWith("%");
        boolean right = pattern.endsWith("%");

        if (!left && !right) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        String text = value.asText();
        String patternValue = pattern.substring(left ? 1 : 0, text.length() - (right ? 1 : 0));
        if (left && right) {
            Assert.isFalse(text.contains(patternValue), () -> new ParamsException(msg));
            return true;
        }

        Assert.isTrue(left ? text.startsWith(patternValue) : text.endsWith(patternValue), () -> new ParamsException(msg));
        return true;
    }
}
