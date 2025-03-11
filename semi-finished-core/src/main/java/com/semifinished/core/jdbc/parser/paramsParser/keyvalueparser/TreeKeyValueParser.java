package com.semifinished.core.jdbc.parser.paramsParser.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.CommonParser;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.Tree;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 树结构规则解析
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

        Tree tree = sqlDefinition.getTree();

        if (tree == null) {
            tree = new Tree();
            sqlDefinition.setTree(tree);
        }
        Assert.isBlank(tree.getParent(), () -> new ParamsException("树查询parent值重复"));
        Assert.isBlank(tree.getChildren(), () -> new ParamsException("树查询children值重复"));
        Assert.isBlank(tree.getId(), () -> new ParamsException("树查询id值重复"));

        List<Column> columns = QuerySqlCombiner.queryColumns(sqlDefinition);

        Set<String> columnsSet = columns.stream()
                .filter(column -> !column.isDisabled())
                .filter(column -> table.equals(column.getTable()))
                .map(Column::getColumn)
                .collect(Collectors.toSet());


        String parent = setTree(sqlDefinition, columnsSet, value, "parent");
        Assert.notBlank(parent, () -> new ParamsException("树查询parent值不能为空"));
        tree.setParent(parent);

        String id = setTree(sqlDefinition, columnsSet, value, "id");
        Assert.notBlank(id, () -> new ParamsException("树查询id值不能为空"));
        tree.setId(id);


        String children = value.path("children").asText("children");
        Assert.notBlank(children, () -> new ParamsException("树查询children值不能为空"));
        tree.setChildren(children);


        return true;
    }

    private String setTree(SqlDefinition sqlDefinition,
                           Set<String> columnsSet,
                           JsonNode value,
                           String key) {
        String column = value.path(key).asText(key);
        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), sqlDefinition.getTable(), column);
        if (columnsSet.add(column)) {
            String treeColumn = tableUtils.uniqueAlias("tree_" + key + "_");
            sqlDefinition.addColumn(sqlDefinition.getTable(), column, treeColumn);
            sqlDefinition.addExcludeColumns("", treeColumn);
            return treeColumn;
        }
        return column;
    }


    @Override
    public int getOrder() {
        return 1000;
    }
}
