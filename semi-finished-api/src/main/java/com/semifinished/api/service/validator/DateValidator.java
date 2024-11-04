package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DateValidator implements Validator {

    @Override
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!pattern.startsWith("date")) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        String datePattern = pattern.substring(4);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);

        try {
            dateTimeFormatter.parse(value.asText());
        } catch (Exception e) {
            throw new ParamsException(msg);
        }
        return true;
    }
}
