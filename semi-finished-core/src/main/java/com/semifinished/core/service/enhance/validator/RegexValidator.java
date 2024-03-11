package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RegexValidator implements Validator {
    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        char[] chars = pattern.toCharArray();
        if (!(chars[0] == '/' && chars[chars.length - 1] == '/')) {
            return false;
        }
        String p = pattern.substring(1, chars.length - 1);
        Pattern compile = Pattern.compile(p);

        Assert.isFalse(compile.matcher(value).matches(), () -> new ParamsException(msg));
        return true;
    }
}
