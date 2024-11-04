package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;

/**
 * 匹配字符串校验
 * "match:a,b,c":"" ->参数只能是a，b，c其中一个
 * "nomatch:a,b,c""" ->参数不能是a，b，c其中一个
 */
@Component
public class MatchValidator implements Validator {
    @Override
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {

        //是否是匹配规则
        boolean match = true;
        if (pattern.startsWith("match:")) {
            pattern = pattern.substring(6);
        } else if (pattern.startsWith("nomatch:")) {
            pattern = pattern.substring(8);
            match = false;
        } else {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        for (String v : pattern.trim().split(",")) {
            Assert.isFalse(!match ^ v.trim().equals(value.asText()), () -> new ParamsException(msg));
        }

        return true;
    }
}
