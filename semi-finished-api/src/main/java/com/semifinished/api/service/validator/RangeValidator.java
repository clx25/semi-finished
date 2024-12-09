package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 判断是否符合长度范围
 * <10   小于10
 * <=10  小于等于10
 * 10<   大于10
 * 10<=  大于等于10
 * <p>
 * 判断是否符合数字范围
 * len<5 长度小于5
 * len<=10  长度小于等于5
 * 10<len  长度大于5
 * 10<=len 长度大于等于5
 */
@Component
public class RangeValidator implements Validator {
    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {



        boolean left = pattern.startsWith("len");
        boolean right = pattern.endsWith("len");

        String finalPattern = pattern;
        Assert.isTrue(left && right, () -> new ConfigException("参数校验配置错误：" + finalPattern));

        pattern = pattern.substring(left ? 3 : 0, right ? pattern.length() - 3 : pattern.length()).trim();
        char[] charArray = pattern.toCharArray();
        boolean l = parseLeft(charArray, field, value, pattern, msg, left ^ right);
        boolean r = parseRight(charArray, field, value, pattern, msg, left ^ right);
        return l || r;

    }

    private boolean parseLeft(char[] charArray, String field, JsonNode value, String pattern, String msg, boolean len) {
        if (charArray[0] != '<' && charArray[0] != '>') {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        boolean lg = charArray[0] == '>'; // 是否大于

        boolean eq = charArray[1] == '='; // 是否等于

        String patternValue = pattern.substring(eq ? 2 : 1);


        compare(field, value.asText(), msg, eq, lg, patternValue.trim(), len);
        return true;
    }


    private boolean parseRight(char[] charArray, String field, JsonNode value, String pattern, String msg, boolean len) {
        int index = charArray.length - 1;
        boolean eq = charArray[index] == '=';
        int lgIndex = eq ? index - 1 : index;

        if (charArray[lgIndex] != '<' && charArray[lgIndex] != '>') {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        boolean lg = charArray[lgIndex] == '<';

        String patternValue = pattern.substring(0, lgIndex);

        compare(field, value.asText(), msg, eq, lg, patternValue.trim(), len);
        return true;
    }

    private void compare(String field, String value, String msg, boolean eq, boolean lg, String patternValue, boolean len) {
        Assert.isFalse(ParamsUtils.isNumber(patternValue), () -> new ConfigException("参数校验范围规则配置错误：" + patternValue));
        Assert.isFalse(len || ParamsUtils.isNumber(value), () -> new ParamsException("只能为数字:" + field));
        int i;
        if (len) {
            i = -1 * Integer.valueOf(patternValue).compareTo(value.length());
        } else {
            i = Double.valueOf(value).compareTo(Double.valueOf(patternValue));
        }


        if (lg) {
            Assert.isFalse(eq ? (i >= 0) : (i > 0), () -> new ParamsException(msg));
        } else {
            Assert.isFalse(eq ? (i <= 0) : i < 0, () -> new ParamsException(msg));
        }
    }

}
