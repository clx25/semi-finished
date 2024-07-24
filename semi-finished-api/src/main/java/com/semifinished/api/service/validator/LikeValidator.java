package com.semifinished.api.service.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

@Component
public class LikeValidator implements Validator {
    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        boolean left = pattern.startsWith("%");
        boolean right = pattern.endsWith("%");

        if (!left && !right) {
            return false;
        }

        String patternValue = pattern.substring(left ? 1 : 0, value.length() - (right ? 1 : 0));
        if (left && right) {
            Assert.isFalse(value.contains(patternValue), () -> new ParamsException(msg));
            return true;
        }

        Assert.isTrue(left ? value.startsWith(patternValue) : value.endsWith(patternValue), () -> new ParamsException(msg));
        return true;
    }
}
