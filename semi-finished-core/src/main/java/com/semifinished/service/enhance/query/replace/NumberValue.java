package com.semifinished.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.util.Assert;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class NumberValue implements ValueReplacer {
    private final DecimalFormat decimalFormat = new DecimalFormat();

    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String key, JsonNode value) {
        if (!key.startsWith("num")) {
            return value;
        }

        String pattern = key.substring(3);
        Assert.hasNotText(pattern, () -> new ParamsException("格式化规则错误，缺少数字格式化规则：" + key));
        double d = value.asDouble(0);
        decimalFormat.applyPattern(pattern);
        try {
            return TextNode.valueOf(decimalFormat.format(d));
        } catch (Exception e) {
            throw new ParamsException("格式化规则错误，缺少数字格式化规则：" + key, e);
        }
    }
}
