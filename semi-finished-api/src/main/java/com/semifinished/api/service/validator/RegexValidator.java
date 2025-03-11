package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RegexValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        char[] chars = pattern.toCharArray();
        if (!(chars[0] == '/' && chars[chars.length - 1] == '/')) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        String p = pattern.substring(1, chars.length - 1);
        Pattern compile = Pattern.compile(p);

        Assert.isTrue(compile.matcher(value.asText()).matches(), () -> new ParamsException(msg));
        return true;
    }
}
