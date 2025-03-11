package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.api.excetion.ApiException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * 范围校验器
 * 支持格式：
 * >6, >7, >=19, <4, <9, <=10
 * 6>, 5>, 9>=, 20<, 20<=
 */
@Component
public class RangeValidator implements Validator {

    @Override
    public boolean beforeParse(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        char[] chars = pattern.toCharArray();

        BiPredicate<Double, Double> predicate = null;
        boolean gte;
        if (chars[0] == '<') {
            gte = chars[1] == '=';
            predicate = (v, p) -> gte ? v <= p : v < p;
            pattern = pattern.substring(gte ? 2 : 1);
        } else if (chars[0] == '>') {
            gte = chars[1] == '=';
            predicate = (v, p) -> gte ? v >= p : v > p;
            pattern = pattern.substring(gte ? 2 : 1);
        } else if (chars[0] == '=') {
            predicate = Objects::equals;
            pattern = pattern.substring(1);
        } else if (chars[chars.length - 1] == '=') {
            if (chars[chars.length - 2] == '<') {
                predicate = (v, p) -> v >= p;
                pattern = pattern.substring(0, chars.length - 2);
            } else if (chars[chars.length - 2] == '>') {
                predicate = (v, p) -> v <= p;
                pattern = pattern.substring(0, chars.length - 2);
            } else {
                predicate = Objects::equals;
                pattern = pattern.substring(0, chars.length - 1);
            }
        } else if (chars[chars.length - 1] == '<') {
            predicate = (v, p) -> v > p;
            pattern = pattern.substring(0, chars.length - 1);
        } else if (chars[chars.length - 1] == '>') {
            predicate = (v, p) -> v < p;
            pattern = pattern.substring(0, chars.length - 1);
        }
        if (predicate == null) {
            return false;
        }
        validateRange(pattern, value, predicate, field, msg);
        return true;
    }


    private void validateRange(String pattern, JsonNode jsonNode, BiPredicate<Double, Double> predicate, String field, String msg) {
        if (jsonNode == null || !StringUtils.hasText(jsonNode.asText(""))) {
            return;
        }
        Assert.isTrue(ParamsUtils.isNumber(pattern), () -> new ApiException("接口数字范围校验规则错误"));

        Assert.isTrue(jsonNode.isNumber() || ParamsUtils.isNumber(jsonNode.asText()), () -> new ParamsException("%s参数应该是数值", field));

        Assert.isTrue(predicate.test(jsonNode.asDouble(), Double.valueOf(pattern)), () -> new ParamsException(msg));
    }

}
