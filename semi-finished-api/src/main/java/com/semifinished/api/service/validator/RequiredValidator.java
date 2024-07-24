package com.semifinished.api.service.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RequiredValidator implements Validator {
    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if ("required".equals(pattern)) {
            Assert.isFalse(StringUtils.hasText(value), () -> new ParamsException(msg));
            return true;
        }
        return false;
    }
}
