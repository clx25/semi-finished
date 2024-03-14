package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DateValidator implements Validator {

    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!pattern.startsWith("date")) {
            return false;
        }

        String datePattern = pattern.substring(4);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);

        try {
            dateTimeFormatter.parse(value);
        } catch (Exception e) {
            throw new ParamsException(msg);
        }
        return true;
    }
}
