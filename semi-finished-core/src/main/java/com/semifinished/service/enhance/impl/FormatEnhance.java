package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.SelectEnhance;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * 格式化数据，支持对数字和日期进行格式化
 */
@Order(200)
@Component
@RequiredArgsConstructor
public class FormatEnhance implements SelectEnhance {
    private final DecimalFormat decimalFormat = new DecimalFormat();
    private final ObjectMapper objectMapper;
    private final CommonParser commonParser;

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        format(sqlDefinition, page.getRecords());
    }


    private void format(SqlDefinition sqlDefinition, List<ObjectNode> records) {
        if (records.isEmpty()) {
            return;
        }
        JsonNode params = sqlDefinition.getRawParams();
        List<Column> columns = sqlDefinition.getColumns();
        Iterator<String> names = params.fieldNames();
        String table = sqlDefinition.getTable();
        while (names.hasNext()) {
            String name = names.next();
            if (name == null || name.length() < 2 || !name.startsWith("#")) {
                continue;
            }
            JsonNode fieldsNode = params.get(name);
            Assert.isTrue(fieldsNode.isMissingNode() || fieldsNode.isNull(), () -> new ParamsException(name + "规则错误"));
            String[] fields = fieldsNode.asText().split(",");

            String format = name.substring(1);

            //#n表示格式化数字类型
            if (format.startsWith("num")) {
                doFormat(table, fields, columns, records, (node) -> {
                    String pattern = format.substring(3);
                    double d = node.asDouble();
                    decimalFormat.applyPattern(pattern);
                    return TextNode.valueOf(decimalFormat.format(d));
                });
            }
            //#t表示格式化时间类型
            else if (format.startsWith("time")) {
                doFormat(table, fields, columns, records, (node) -> {
                    String pattern = format.substring(4);
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String date = LocalDate.parse(node.asText(), dateTimeFormatter)
                            .format(DateTimeFormatter.ofPattern(pattern));
                    return TextNode.valueOf(date);
                });
            }
            //#d表示默认
            else if (format.startsWith("def")) {
                doFormat(table, fields, columns, records, (node) -> TextNode.valueOf((node == null || node.isNull()) ? format.substring(3) : node.asText()));
            }
            //#j表示json
            else if (format.startsWith("json")) {
                doFormat(table, fields, columns, records, (node) -> {
                    try {
                        return objectMapper.readTree(node.asText(""));
                    } catch (JsonProcessingException e) {
                        throw new ParamsException("json规则执行失败", e);
                    }
                });
            }
            //#b表示转为boolean类型，空数组，空对象，空字符串，null,false,不区分大小的"false"字符串，都返回false，其他返回true
            else if (format.startsWith("boolean")) {
                doFormat(table, fields, columns, records, (node) -> {
                    if (node.isBoolean()) {
                        return node;
                    }
                    if (node.isArray()) {
                        return BooleanNode.valueOf(!node.isEmpty());
                    }
                    String value = node.asText(null);
                    if (!StringUtils.hasText(value)) {
                        return BooleanNode.getFalse();
                    }
                    return "false".equalsIgnoreCase(value) ? BooleanNode.getFalse() : BooleanNode.getTrue();
                });

            }

        }
    }

    private void doFormat(String table, String[] fields, List<Column> columns, List<ObjectNode> objectNodes, Function<JsonNode, JsonNode> function) {

        for (ObjectNode objectNode : objectNodes) {
            for (String field : fields) {
                String column = commonParser.getActualColumn(table, field);
                String key = columns.stream()
                        .filter(col -> col.getTable().equals(table) && col.getColumn().equals(column))
                        .map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn()))
                        .findFirst().orElse("");
                JsonNode jsonNode = objectNode.get(key);
                if (jsonNode != null) {
                    objectNode.set(key, function.apply(jsonNode));
                    continue;
                }
                if (!objectNode.has("items")) {
                    continue;
                }
                ArrayNode arrayNode = objectNode.withArray("items");

                List<ObjectNode> items = objectMapper.convertValue(arrayNode, new TypeReference<List<ObjectNode>>() {
                });
                arrayNode.removeAll().addAll(items);
                doFormat(table, fields, columns, items, function);
            }
        }
    }
}
