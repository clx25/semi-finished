package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.CommonParser;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.pojo.ValueReplace;
import com.semifinished.core.service.enhance.query.replace.ValueReplacer;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 对返回数据进行替换
 */
@Order(0)
@Component
@RequiredArgsConstructor
public class ValueReplaceEnhance implements AfterQueryEnhance {

    private final CommonParser commonParser;
    private final List<ValueReplacer> valueReplacers;

    @Override
    public void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();
        if (records.isEmpty()) {
            return;
        }

        List<ValueReplace> valueReplaces = QuerySqlCombiner.valueReplacesAll(sqlDefinition);
        if (valueReplaces.isEmpty()) {
            return;
        }

        for (ValueReplace valueReplace : valueReplaces) {
            replaceValue(sqlDefinition, valueReplace, records);
        }
    }

    /**
     * 替换数据
     *
     * @param sqlDefinition SQL定义信息
     * @param valueReplace  替换规则
     * @param objectNodes   被替换的数据集合
     */
    private void replaceValue(SqlDefinition sqlDefinition, ValueReplace valueReplace, List<ObjectNode> objectNodes) {
        String table = valueReplace.getTable();
        List<Column> columns = QuerySqlCombiner.columnsAll(sqlDefinition);

        List<String> recodeKeys = new ArrayList<>();
        for (String field : valueReplace.getColumn().split(",")) {
            //获取前端字段的实际字段
            String column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, field);
            String recodeKey = columns.stream()
                    .filter(col -> col.getTable().equals(table))
                    .filter(col -> col.getColumn().equals(column))
                    .map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn()))
                    .findFirst().orElse("");
            Assert.notBlank(recodeKey, () -> new ParamsException("字段不存在：" + field));
            recodeKeys.add(recodeKey);
        }
        String pattern = valueReplace.getPattern();
        for (ObjectNode objectNode : objectNodes) {
            replaceObject(sqlDefinition, pattern, recodeKeys, objectNode);
        }
    }

    /**
     * 执行替换
     *
     * @param sqlDefinition SQL定义信息
     * @param pattern       替换规则
     * @param recodeKeys    被替换数据的key
     * @param objectNode    被替换的数据对象
     */
    private void replaceObject(SqlDefinition sqlDefinition, String pattern, List<String> recodeKeys, ObjectNode objectNode) {
        for (String recodeKey : recodeKeys) {

            JsonNode value = objectNode.get(recodeKey);

            value = executeReplacer(sqlDefinition, pattern, value);
            objectNode.set(recodeKey, value);

        }
    }


    private JsonNode executeReplacer(SqlDefinition sqlDefinition, String pattern, JsonNode value) {
        for (ValueReplacer valueReplacer : valueReplacers) {
            value = valueReplacer.replace(sqlDefinition, pattern, value);
        }
        return value;
    }
}
