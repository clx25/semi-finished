package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

/**
 * =value    -->等于value
 * !=value   --->不等于value
 * !=        --->不等于“”
 * =         --->等于“”
 * !         ---不等于null
 * null      ---等于null
 */
@Component
public class BlankValidator implements Validator {
    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {

        if ("text".equals(pattern)) {
            Assert.hasNotText(value, () -> new ParamsException(msg));
            return true;
        }
        if (!pattern.startsWith("!")) {
            return false;
        }


        if (pattern.length() == 1) {
            Assert.isTrue("".equals(value), () -> new ParamsException(msg));
            return true;
        }

        String patternValue = pattern.substring(1).trim();
        if ("null".equals(patternValue)) {
            Assert.isTrue(value == null, () -> new ParamsException(msg));
            return true;
        }

        for (String v : patternValue.split(",")) {
            Assert.isTrue(v.trim().equals(value), () -> new ParamsException(msg));
        }

        return true;
    }
}
