package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PhoneValidator implements Validator {
    private static final Pattern PATTERN = Pattern.compile("1\\d{10}]");

    @Override
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"phone".equalsIgnoreCase(pattern)) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        Assert.isFalse(PATTERN.matcher(value.asText()).matches(), () -> new ParamsException(msg));
        return true;
    }
}
