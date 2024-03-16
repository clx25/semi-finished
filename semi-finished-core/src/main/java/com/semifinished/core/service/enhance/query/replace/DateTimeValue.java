package com.semifinished.core.service.enhance.query.replace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeValue implements ValueReplacer {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public JsonNode replace(SqlDefinition sqlDefinition, String key, JsonNode value) {

        if (!key.startsWith("time")||value==null) {
            return value;
        }
        String text = value.asText(null);
        if (!StringUtils.hasText(text)) {
            return value;
        }
        String pattern = key.substring(4);
        Assert.hasNotText(pattern, () -> new ParamsException("缺少时间格式化规则：" + key));

        try {
            String date = LocalDate.parse(text, text.contains(":") ? dateTimeFormatter : dateFormatter)
                    .format(DateTimeFormatter.ofPattern(pattern));
            return TextNode.valueOf(date);
        } catch (Exception e) {
            throw new ParamsException("时间格式化规则错误：" + key, e);
        }
    }
}
