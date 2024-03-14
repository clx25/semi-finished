package com.semifinished.core.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
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
        Assert.hasNotText(pattern, () -> new ParamsException("缺少数字格式化规则：" + key));
        double d = value.asDouble(0);

        try {
            decimalFormat.applyPattern(pattern);
        } catch (Exception e) {
            throw new ParamsException("数字格式化规则错误：" + key);
        }

        return TextNode.valueOf(decimalFormat.format(d));

    }
}
