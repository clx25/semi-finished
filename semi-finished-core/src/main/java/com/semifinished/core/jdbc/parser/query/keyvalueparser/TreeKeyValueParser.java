package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 树结构规则解析
 * todo 暂不支持多表数据转树结构
 * 如果parent，id字段没有存在于查询字段中，如何处理？
 * 1. 抛出异常，提示需要这两个字段（是否合理？）
 * 2. 添加到查询字段中，返回前端前删除（多表数据时如何确定字段对应的表？）
 */
@Component
@AllArgsConstructor
public class TreeKeyValueParser implements KeyValueParamsParser {

    private final CommonParser commonParser;
    private final TableUtils tableUtils;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"^".equals(key.trim())) {
            return false;
        }
        ObjectNode expand = sqlDefinition.getExpand();
        expand = expand.with("^");
        List<Column> columns = QuerySqlCombiner.queryColumns(sqlDefinition);

        Set<String> columnsSet = columns.stream().filter(column -> !column.isDisabled()).filter(column -> table.equals(column.getTable())).map(Column::getColumn).collect(Collectors.toSet());


        parseValue(columnsSet, sqlDefinition, expand, value, "parent");
        parseValue(columnsSet, sqlDefinition, expand, value, "id");
        if (!value.has("children")) {
            return true;
        }
        String children = value.path("children").asText("children");
        Assert.hasNotText(children, () -> new ParamsException("树查询children值不能为空"));
        Assert.isFalse(columnsSet.add(children), () -> new ParamsException("树规则字段已存在：" + children));

        addExpand(expand, "children", children);

        return true;
    }

    private static void addExpand(ObjectNode expand, String key, String column) {
        Assert.isTrue(expand.has(key), () -> new ParamsException("树规则存在多个parent"));
        expand.put(key, column);
    }

    private void parseValue(Set<String> columnsSet, SqlDefinition sqlDefinition, ObjectNode expand, JsonNode params, String key) {
        String column = getValue(sqlDefinition, params, key);
        if (column == null) {
            return;
        }

        if (columnsSet.add(column)) {
            String treeColumn = tableUtils.uniqueAlias("tree_" + key + "_");
            sqlDefinition.addColumn(sqlDefinition.getTable(), column, treeColumn);
            sqlDefinition.addExcludeColumns("", treeColumn);
            column = treeColumn;
        }
        addExpand(expand, key, column);
    }

    private String getValue(SqlDefinition sqlDefinition, JsonNode params, String key) {
        if (!params.has(key)) {
            return null;
        }
        JsonNode jsonNode = params.get(key);
        String column = jsonNode == null ? key : jsonNode.asText();
        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), sqlDefinition.getTable(), column);
        Assert.hasNotText(column, () -> new ParamsException("树查询" + key + "值不能为空"));
        return column;
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
