package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

/**
 * 邮箱校验，使用了hibernate的内置邮箱校验
 */
@Component
public class EmailValidator implements Validator {


    @Override
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"email".equalsIgnoreCase(pattern)) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        boolean valid = new org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator().isValid(value.asText(), null);
        Assert.isFalse(valid, () -> new ParamsException(msg));
        return true;
    }
}
