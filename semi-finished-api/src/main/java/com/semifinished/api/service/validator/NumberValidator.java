package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import org.springframework.stereotype.Component;

/**
 * 数字格式校验器
 * 格式：number
 */
@Component
public class NumberValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"number".equals(pattern)) {
            return false;
        }

        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }

        Assert.isFalse(ParamsUtils.isNumber(value.asText()), () -> new ParamsException(msg));

        return true;
    }
}
