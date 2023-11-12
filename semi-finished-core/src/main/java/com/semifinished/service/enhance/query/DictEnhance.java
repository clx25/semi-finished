package com.semifinished.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
public class DictEnhance implements AfterQueryEnhance {

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
            definition.setMaxPageSize(0);
            int rowStart = definition.getRowStart();
            int rowEnd = definition.getRowEnd();
            definition.setRowStart(0);
            definition.setRowEnd(0);

            Object data = enhanceService.selectNoParse(definition);
            List<ObjectNode> records;

            if (data instanceof Page) {
                records = ((Page) data).getRecords();
            } else {
                records = (List<ObjectNode>) data;
            }

            combine(definition, page.getRecords(), records, rowStart, rowEnd);
        }

    }

    private void combine(SqlDefinition definition, List<ObjectNode> records, List<ObjectNode> replaces, int rowStart, int rowEnd) {
        Pair<String, String> joinOn = definition.getJoinOn();
        String left = joinOn.getFirst();
        String right = joinOn.getSecond();


        Map<String, ObjectNode> map = new HashMap<>();
        for (ObjectNode record : records) {
            map.put(record.path(left).asText(), record);
        }
        for (ObjectNode replace : replaces) {
            ObjectNode record = map.get(replace.path(right).asText());
            if (record == null) {
                continue;
            }

            replace.fields().forEachRemaining(entry -> {
                JsonNode node = entry.getValue();
                String key = entry.getKey();
                ArrayNode jsonNodes = record.withArray(key);
                if (node instanceof ArrayNode) {
                    jsonNodes.addAll((ArrayNode) node);
                    return;
                }
                jsonNodes.add(node);
            });


        }
        List<Column> columns = definition.getColumns();
        List<String> columnsField = columns.stream().map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn())).collect(Collectors.toList());
        List<Column> excludeColumns = definition.getExcludeColumns();
        for (ObjectNode record : records) {
            for (String field : columnsField) {
                if (rowStart <= 0) {
                    break;
                }
                if (rowEnd == 0) {
                    JsonNode jsonNode = record.remove(field);
                    record.set(field, jsonNode.get(rowStart - 1));
                    continue;
                }
                ArrayNode jsonNodes = record.withArray(field);
                ArrayNode rows = JsonNodeFactory.instance.arrayNode();
                for (int i = 1; i < jsonNodes.size() + 1; i++) {
                    if (i <= rowEnd && i >= rowStart) {
                        rows.add(jsonNodes.get(i - 1));
                    }
                }
                record.set(field, rows);
            }

            if (CollectionUtils.isEmpty(excludeColumns)) {
                continue;
            }
            for (Column excludeColumn : excludeColumns) {
                record.remove(excludeColumn.getColumn());
            }
        }


    }
}
