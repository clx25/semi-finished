package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.CodeException;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlCombiner;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.service.EnhanceService;
import com.semifinished.service.enhance.SelectEnhance;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表字典查询
 * <pre>
 *     {
 *         "col:":{
 *             "@tb":"tb1",
 *             "@on":"col2"
 *         }
 *     }
 * </pre>
 */
@Component
@Order(-500)
@AllArgsConstructor
public class DictEnhance implements SelectEnhance {

    private final IdGenerator idGenerator;
    @Resource
    private EnhanceService enhanceService;

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        List<Column> columns = new ArrayList<>();
        populateColumns(columns, sqlDefinition);
        Set<String> uniqueSet = new HashSet<>();
        for (Column column : columns) {
            String alias = column.getAlias();
            String addColumn = ParamsUtils.hasText(alias, column.getColumn());
            Assert.isFalse(uniqueSet.add(addColumn), () -> new ParamsException("字段重复:" + addColumn));
        }
    }

    private void populateColumns(List<Column> columns, SqlDefinition sqlDefinition) {
        List<Column> columnList = SqlCombiner.columnAggregationAll(sqlDefinition);
        columns.addAll(columnList);
        List<SqlDefinition> dicts = sqlDefinition.getDict();
        if (CollectionUtils.isEmpty(dicts)) {
            return;
        }
        for (SqlDefinition dict : dicts) {
            populateColumns(columns, dict);
        }
    }

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<SqlDefinition> dict = sqlDefinition.getDict();
        if (CollectionUtils.isEmpty(dict)) {
            return;
        }
        for (SqlDefinition definition : dict) {
            Pair<String, String> joinOn = definition.getJoinOn();
            String key = joinOn.getKey();
            String value = joinOn.getValue();

            //获取关联字段的数据集合
            List<String> args = page.getRecords()
                    .stream()
                    .flatMap(node -> {
                        JsonNode jsonNode = node.get(key);
                        if (!(jsonNode instanceof ArrayNode)) {
                            return Stream.of(jsonNode.asText());
                        }
                        String[] nodes = new String[jsonNode.size()];
                        for (int i = 0; i < jsonNode.size(); i++) {
                            nodes[i] = jsonNode.get(i).asText();
                        }
                        return Stream.of(nodes);
                    })
                    .collect(Collectors.toList());

            //获取查询的where字段
            String inCol = definition.getColumns()
                    .stream()
                    .filter(col -> value.equals(col.getAlias()) || value.equals(col.getColumn()))
                    .map(Column::getColumn)
                    .findFirst()
                    .orElseThrow(() -> new CodeException("表字典查询未找到in查询字段"));

            //构建占位符名称
            String argName = TableUtils.uniqueAlias(idGenerator, "in_" + definition.getTable() + "_" + inCol);

            //构建查询条件
            ValueCondition valueCondition = ValueCondition.builder()
                    .table(definition.getTable())
                    .column(inCol)
                    .combination("and")
                    .argName(argName)
                    .condition("in ( :" + argName + ") ")
                    .value(args)
                    .build();

            definition.addColumnValue(valueCondition);

            Object data = enhanceService.selectNoParse(definition);
            List<ObjectNode> records;

            if (data instanceof Page) {
                records = ((Page) data).getRecords();
            } else {
                records = (List<ObjectNode>) data;
            }

            combine(definition, page.getRecords(), records);
        }

    }

    private void combine(SqlDefinition definition, List<ObjectNode> records, List<ObjectNode> replaces) {
        Pair<String, String> joinOn = definition.getJoinOn();
        String key = joinOn.getFirst();
        String value = joinOn.getSecond();

        List<Column> excludeColumns = definition.getExcludeColumns();
        Map<String, ObjectNode> map = new HashMap<>();
        for (ObjectNode record : records) {
            map.put(record.path(key).asText(), record);
        }
        for (ObjectNode replace : replaces) {
            ObjectNode record = map.get(replace.path(value).asText());
            if (record == null) {
                continue;
            }

            replace.fields().forEachRemaining(entry -> {
                JsonNode node = entry.getValue();
                ArrayNode jsonNodes = record.withArray(entry.getKey());
                if (node instanceof ArrayNode) {
                    jsonNodes.addAll((ArrayNode) node);
                    return;
                }
                jsonNodes.add(node);
            });
            if (CollectionUtils.isEmpty(excludeColumns)) {
                continue;
            }
            for (Column excludeColumn : excludeColumns) {
                record.remove(excludeColumn.getColumn());
            }
        }


    }
}
