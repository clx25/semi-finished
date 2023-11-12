package com.semifinished.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.query.replace.ValueReplace;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 对返回数据进行替换
 */
@Order(200)
@Component
@RequiredArgsConstructor
public class ValueReplaceEnhance implements AfterQueryEnhance {

    private final CommonParser commonParser;
    private final List<ValueReplace> valueReplaces;

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        if (records.isEmpty()) {
            return;
        }
        JsonNode params = sqlDefinition.getRawParams();
        Iterator<String> names = params.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (name == null || name.length() < 2 || !name.startsWith("#")) {
                continue;
            }
            JsonNode fieldsNode = params.get(name);
            Assert.isTrue(fieldsNode.isMissingNode() || fieldsNode.isNull(), () -> new ParamsException(name + "规则错误"));
            String[] fields = fieldsNode.asText().split(",");

            String pattern = name.substring(1);
            replaceValue(sqlDefinition, pattern, fields, records);

        }
    }


    private void replaceValue(SqlDefinition sqlDefinition, String pattern, String[] fields, List<ObjectNode> objectNodes) {
        String table = sqlDefinition.getTable();
        List<Column> columns = sqlDefinition.getColumns();

        List<String> recodeKeys = new ArrayList<>();
        for (String field : fields) {
            //获取前端字段的实际字段
            String column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, field);
            String recodeKey = columns.stream()
                    .filter(col -> col.getTable().equals(table) && col.getColumn().equals(column))
                    .map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn()))
                    .findFirst().orElse("");
            recodeKeys.add(recodeKey);
        }
        for (ObjectNode objectNode : objectNodes) {
            doReplace(sqlDefinition, pattern, recodeKeys, objectNode);
        }
    }

    private void doReplace(SqlDefinition sqlDefinition, String pattern, List<String> recodeKeys, ObjectNode objectNode) {
        for (String recodeKey : recodeKeys) {

            JsonNode jsonNode = objectNode.get(recodeKey);
            if (jsonNode == null) {
                continue;
            }
            for (ValueReplace valueReplace : valueReplaces) {
                jsonNode = valueReplace.replace(sqlDefinition, pattern, jsonNode);
                objectNode.set(recodeKey, jsonNode == null ? NullNode.instance : jsonNode);
            }

//                List<ObjectNode> items = objectMapper.convertValue(arrayNode, new TypeReference<List<ObjectNode>>() {
//                });
//                arrayNode.removeAll().addAll(items);
//                doFormat(sqlDefinition, fields, items, function);
        }
    }
}
