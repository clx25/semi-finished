package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlCombiner;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.SqlExecutorHolder;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.service.enhance.SelectEnhance;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现group by一对多查询
 * 第一次查询：把group by没有覆盖的查询字段去除，如果查询字段中没有group by的字段，那么添加到查询字段，也添加到排除字段，执行查询
 * 第二次查询：从查询的列表取出group by字段的结果，作为IN查询的参数。把之前的查询字段与group by字段合并，去除group by规则，执行查询
 * 合并：根据group by字段进行合并，group by没有覆盖的查询字段是集合形式
 */
@Order(100)
@Component
@AllArgsConstructor
public class GroupByEnhance implements SelectEnhance {
    private final SqlExecutorHolder sqlExecutorHolder;
    private final IdGenerator idGenerator;

    /**
     * 判断group by的字段是否覆盖了所有查询字段，用以决定是否执行一对多查询
     *
     * @param groupBy group by字段
     * @param columns 查询字段
     * @return true表示需要执行一对多查询，false表示不需要
     */
    private static boolean isMany(List<Column> groupBy, List<Column> columns) {
        return columns.stream()
                .anyMatch(col -> groupBy.stream()
                        .noneMatch(group -> group.getTable().equals(col.getTable()) &&
                                group.getColumn().equals(col.getColumn())
                        )
                );
    }

    /**
     * 获取查询字段中有，但是group by中没有的字段，这些字段就是需要匹配后去合并的字段
     *
     * @param columns 查询字段
     * @param groupBy group by字段
     * @return 合并字段集合
     */
    private static List<String> getMergeColumns(List<Column> columns, List<Column> groupBy) {
        return columns.stream()
                .filter(col -> groupBy.stream().noneMatch(column -> col.getTable().equals(column.getTable()) && col.getColumn().equals(column.getColumn())))
                .map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn()))
                .collect(Collectors.toList());
    }

    /**
     * 筛选出group by中有，但查询字段中没有的字段，合并在一起作为第二次查询的字段
     *
     * @param columns 查询字段
     * @param groupBy group by字段
     */
    private static void secondQueryColumns(List<Column> columns, List<Column> groupBy) {
        groupBy.stream()
                .filter(col -> columns.stream()
                        .noneMatch(column -> col.getTable().equals(column.getTable()) && col.getColumn().equals(column.getColumn()))
                )
                .forEach(columns::add);
    }

    /**
     * 合并数据
     *
     * @param records        第一次查询数据列表
     * @param groupBy        group by字段
     * @param mergeColumns   合并字段
     * @param matchColumns   匹配字段
     * @param noGroupRecords 第二次查询数据列表
     */
    private static void merge(List<ObjectNode> records, List<Column> groupBy, List<String> mergeColumns, List<String> matchColumns, List<ObjectNode> noGroupRecords) {
        for (ObjectNode record : records) {
            Iterator<ObjectNode> iterator = noGroupRecords.iterator();
            while (iterator.hasNext()) {
                ObjectNode noGroupRecord = iterator.next();
                //匹配group by字段，只有所有字段对应的值都相同才表示是对应的值
                boolean match = matchColumns.stream()
                        .allMatch(matchColumn -> noGroupRecord.get(matchColumn).equals(record.get(matchColumn)));
                if (!match) {
                    continue;
                }
                //把对应的数据以数组的形式添加到返回列表
                for (String mergeColumn : mergeColumns) {
                    record.withArray(mergeColumn).add(noGroupRecord.get(mergeColumn));
                }
                iterator.remove();
            }
            //排除自动添加的匹配字段
            for (Column column : groupBy) {
                if (StringUtils.hasText(column.getAlias())) {
                    record.remove(column.getAlias());
                }
            }
        }
    }

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        JsonNode jsonNode = sqlDefinition.getParams().path("@group");
        return !jsonNode.isMissingNode();
    }

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        List<Column> groupBy = sqlDefinition.getGroupBy();
        if (groupBy == null || groupBy.isEmpty()) {
            return;
        }
        List<Column> columns = SqlCombiner.columnsAll(sqlDefinition);

        //判断是否一对多查询
        boolean many = isMany(groupBy, columns);
        sqlDefinition.setToMany(many);

        //查询字段与group by字段取并集
        union(sqlDefinition, groupBy, columns);
    }

    /**
     * 把group by 字段添加到第一次查询里面，如果之前不存在，那么添加到group by别名里
     * 为了避免group by 字段名与已有的别名重复，需要使用别名
     *
     * @param sqlDefinition SQL定义信息
     * @param groupBy       group by字段
     * @param columns       查询字段
     */
    private void union(SqlDefinition sqlDefinition, List<Column> groupBy, List<Column> columns) {
        groupBy.stream()
                .filter(pair -> columns.stream()
                        .noneMatch(col -> col.getTable().equals(pair.getTable()) &&
                                col.getColumn().equals(pair.getColumn())
                        )
                )
                .forEach(col -> {
                    String alias = TableUtils.uniqueAlias(idGenerator, "group_by_alias");
                    sqlDefinition.addColumn(col.getTable(), col.getColumn(), alias);
                    col.setAlias(alias);
                });
    }

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        if (records.isEmpty() || !sqlDefinition.isToMany()) {
            return;
        }
        combine(sqlDefinition, records);
    }

    private void combine(SqlDefinition sqlDefinition, List<ObjectNode> records) {
        List<Column> columns = SqlCombiner.columnsAll(sqlDefinition);
        List<Column> groupBy = sqlDefinition.getGroupBy();

        //获取合并字段
        List<String> mergeColumns = getMergeColumns(columns, groupBy);

        //整合第二次查询的字段
        secondQueryColumns(columns, groupBy);

        //获取匹配字段
        List<String> matchColumns = matchColumns(columns, groupBy);

        //获取第二次查询的值
        List<Object[]> argValue = argsValue(records, matchColumns);

        //构建查询条件
        SqlDefinition sqlDef = buildSecondSqlSqlDefinition(sqlDefinition, columns, groupBy, argValue);

        //获取查询SQL
        String sql = SqlCombiner.creatorSqlWithoutLimit(sqlDef);

        //执行查询
        List<ObjectNode> noGroupRecords = sqlExecutorHolder.dataSource(sqlDef.getDataSource()).list(sql, SqlCombiner.getArgs(sqlDef));

        //合并数据
        merge(records, groupBy, mergeColumns, matchColumns, noGroupRecords);

    }

    /**
     * in查询的数据
     *
     * @param records      第一次查询数据列表
     * @param matchColumns 匹配字段
     * @return in查询数据集合
     */
    private List<Object[]> argsValue(List<ObjectNode> records, List<String> matchColumns) {
        List<Object[]> argsValue = new ArrayList<>();


        //获取in查询的数据
        for (ObjectNode record : records) {
            Object[] arg = new Object[matchColumns.size()];
            for (int i = 0; i < matchColumns.size(); i++) {
                arg[i] = record.path(matchColumns.get(i)).asText();
            }
            argsValue.add(arg);
        }
        return argsValue;
    }

    /**
     * 获取group by字段对应的查询实际列名,作为合并时的匹配字段
     *
     * @param columns 查询字段
     * @param groupBy group by字段
     * @return 匹配字段
     */
    private List<String> matchColumns(List<Column> columns, List<Column> groupBy) {
        List<String> matchColumns = new ArrayList<>();
        for (Column column : groupBy) {
            for (Column col : columns) {
                if (col.getTable().equals(column.getTable()) && col.getColumn().equals(column.getColumn())) {
                    String matchColumn = ParamsUtils.hasText(col.getAlias(), col.getColumn());
                    matchColumns.add(matchColumn);
                    break;
                }
            }
        }
        return matchColumns;
    }

    /**
     * 构建一个in查询SqlDefinition
     *
     * @param sqlDefinition SQL定义信息
     * @param columns       查询字段
     * @param groupBy       group by字段
     * @param argValue      查询数据集合
     * @return in查询SqlDefinition
     */
    private SqlDefinition buildSecondSqlSqlDefinition(SqlDefinition sqlDefinition, List<Column> columns, List<Column> groupBy, List<Object[]> argValue) {
        //构建in查询，拷贝一个新的SqlDefinition
        //由于是浅拷贝，使用不修改内部对象的方式进行赋值，避免影响原始数据
        SqlDefinition sqlDef = new SqlDefinition(null);
        BeanUtils.copyProperties(sqlDefinition, sqlDef);
        sqlDef.setGroupBy(Collections.emptyList());
        sqlDef.setAggregationFuns(Collections.emptyList());
        sqlDef.setDistinct(false);
        sqlDef.setToMany(false);
        sqlDef.setColumns(columns);
        sqlDef.setValueCondition(null);
        String inColumn = "(" + groupBy.stream().map(col -> col.getTable() + "." + col.getColumn()).collect(Collectors.joining(",")) + ")";
        String argName = TableUtils.uniqueAlias(idGenerator, "group_by_in");
        ValueCondition valueCondition = ValueCondition.builder().column(inColumn).condition("in( :" + argName + ")").argName(argName).build();
        List<ValueCondition> argValueCopy = new ArrayList<>();
        if (sqlDef.getValueCondition() != null) {
            Collections.copy(argValueCopy, sqlDef.getValueCondition());
        }
        valueCondition.setValue(argValue);
        argValueCopy.add(valueCondition);
        sqlDef.setValueCondition(argValueCopy);
        return sqlDef;
    }
}
