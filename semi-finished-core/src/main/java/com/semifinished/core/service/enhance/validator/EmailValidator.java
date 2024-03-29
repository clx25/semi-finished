package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

@Component
public class EmailValidator implements Validator {


    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"email".equalsIgnoreCase(pattern)) {
            return false;
        }

        boolean valid = new org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator().isValid(value, null);
        Assert.isFalse(valid, () -> new ParamsException(msg));
        return true;
    }
}
