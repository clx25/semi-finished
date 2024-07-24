package com.semifinished.api.service.validator;

import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;

@Component
public class BooleanValidator implements Validator {
    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"boolean".equals(pattern)) {
            return false;
        }
        if (value != null) {

        }
        return true;
    }
}
