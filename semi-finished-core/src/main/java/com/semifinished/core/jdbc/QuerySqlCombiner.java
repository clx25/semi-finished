package com.semifinished.core.jdbc;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.AggregationFun;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.pojo.ValueReplace;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 把{@link SqlDefinition}中的数据组合成查询SQL
 */

public class QuerySqlCombiner {


    /**
     * 把{@link SqlDefinition}中的数据组合成sql
     *
     * @return 组装完整的sql, 这个sql会直接进行执行
     */
    public static String query(SqlDefinition sqlDefinition) {
        StringBuilder sql = new StringBuilder(creatorSqlWithoutLimit(sqlDefinition));

        int pageNum = sqlDefinition.getPageNum();

        int pageSize = sqlDefinition.getPageSize();

        if ((pageNum | pageSize) == 0) {
            pageSize = sqlDefinition.getMaxPageSize();
            if (pageSize <= 0) {
                return sql.toString();
            }
            pageNum = 1;
        }

        return sql.append(" limit ")
                .append((pageNum - 1) * pageSize).append(" , ")
                .append(pageSize)
                .toString();
    }


    /**
     * 构建不带分页参数的查询sql
     *
     * @return sql
     */
    public static String creatorSqlWithoutLimit(SqlDefinition sqlDefinition) {

        validUniqueColumns(sqlDefinition);

        return new StringBuilder("select ")
                .append(columns(sqlDefinition))
                .append(" from ")
                .append(table(sqlDefinition))
                .append(" ")
                .append(join(sqlDefinition))
                .append(where(sqlDefinition))
                .append(groupBy(sqlDefinition))
                .append(orderBy(sqlDefinition))
                .append(" ")
                .toString();
    }


    /**
     * 获取group by SQL片段
     *
     * @param sqlDefinition SQL定义信息
     * @return group by SQL片段
     */
    private static StringBuilder groupBy(SqlDefinition sqlDefinition) {

        StringBuilder sql = new StringBuilder();
        if (sqlDefinition.getGroupStatus() == SqlDefinition.GROUP_DISABLE) {
            return sql;
        }
        List<Column> columns = groupByAll(sqlDefinition);
        if (columns.isEmpty()) {
            return sql;
        }
        sql.append(" group by ");

        String groupBy = columns.stream()
                .map(g -> g.getTable() + "." + g.getColumn())
                .collect(Collectors.joining(","));
        sql.append(groupBy).append(" ");

        return sql;
    }


    /**
     * 获取group by 字段
     *
     * @param sqlDefinition SQL定义信息
     * @return group by 字段
     */
    public static List<Column> groupByAll(SqlDefinition sqlDefinition) {
        List<Column> groupByAll = new ArrayList<>();
        integration(sqlDefinition, poll -> {
            List<Column> groupBy = poll.getGroupBy();
            if (groupBy != null) {
                groupByAll.addAll(groupBy);
            }
        }, SqlDefinition::getJoin);
        return groupByAll;
    }


    /**
     * 获取查询的表名或者子表查询
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询的表名或者子表查询
     */
    public static String table(SqlDefinition sqlDefinition) {
        SqlDefinition innerTable = sqlDefinition.getSubTable();
        if (innerTable != null) {
            return " ( " + query(innerTable) + " ) " + sqlDefinition.getTable();
        }
        return sqlDefinition.getTable();
    }


    /**
     * 获取所有普通查询字段+聚合字段
     *
     * @param sqlDefinition SQL定义信息
     * @return 所有普通查询字段+聚合字段
     */
    public static List<Column> columnAggregationAll(SqlDefinition sqlDefinition) {
        List<Column> columnAll = queryColumns(sqlDefinition);
        List<Column> aggregationColumns = aggregationColumns(sqlDefinition);
        Assert.isTrue(!columnAll.isEmpty() &&
                        !aggregationColumns.isEmpty() &&
                        (sqlDefinition.getGroupBy() == null || sqlDefinition.getGroupBy().isEmpty()),
                () -> new ParamsException("同时存在聚合函数与查询字段，缺少group by规则"));
        columnAll.addAll(aggregationColumns);
        Assert.isEmpty(columnAll, () -> new ParamsException("未指定查询字段"));
        return columnAll;
    }


    /**
     * 获取查询的字段并添加去重字段，如果判断有一对多查询，也就是group by的字段没有覆盖查询字段，那么会进行筛选
     * 去除没有覆盖的字段，并会在增强类中补齐
     *
     * @param sqlDefinition SQL定义信息
     * @return 经过筛选后的包含查询字段和去重规则的SQL片段
     */
    private static String columns(SqlDefinition sqlDefinition) {
        List<Column> groupBy = groupByAll(sqlDefinition);
        List<Column> columns = queryColumns(sqlDefinition);
        List<Column> aggregationColumns = aggregationColumns(sqlDefinition);

        Stream<Column> columnsStream = columns.stream();
        //如果有group规则，那么需要查询字段与group字段取交集
        if (sqlDefinition.getGroupStatus() == SqlDefinition.GROUP_NOT_COVER) {
            columnsStream = columnsStream.filter(col -> groupBy.stream()
                    .filter(group -> col.getTable().equals(group.getTable()))
                    .anyMatch(group -> col.getColumn().equals(group.getColumn()))
            );
        }

        //构建查询字段SQL片段
        String queryColumns = Stream.concat(columnsStream, aggregationColumns.stream())
                .map(QuerySqlCombiner::columnItem)
                .collect(Collectors.joining(","));

        return (sqlDefinition.isDistinct() ? " distinct " : "") + queryColumns;
    }

    /**
     * 单个查询字段SQL片段
     *
     * @param col 查询的字段
     * @return 查询字段SQL片段
     */
    private static String columnItem(Column col) {
        return (StringUtils.hasText(col.getTable()) ? (col.getTable() + ".") : "") + col.getColumn() + " " + (StringUtils.hasText(col.getAlias()) ? "`" + col.getAlias() + "`" : "");
    }


    /**
     * 获取所有普通查询字段，不包含聚合字段
     *
     * @param sqlDefinition SQL定义信息
     * @return 所有普通查询字段，不包含聚合字段
     */
    public static List<Column> queryColumns(SqlDefinition sqlDefinition) {

        List<Column> columnAll = new ArrayList<>();

        integration(sqlDefinition, poll -> {
            for (Column column : poll.getColumns()) {
                if (!column.isDisabled()) {
                    columnAll.add(column);
                }
            }

        }, SqlDefinition::getJoin);


        aliasColumns(sqlDefinition, columnAll);

        return columnAll;
    }


    /**
     * 获取参数解析后的所有字段
     *
     * @param sqlDefinition SQL定义信息
     * @return 字段集合
     */
    public static List<Column> columnsAll(SqlDefinition sqlDefinition) {

        List<Column> columnAll = new ArrayList<>();

        integration(sqlDefinition, poll -> {
            for (Column column : poll.getColumns()) {
                if (!column.isDisabled()) {
                    columnAll.add(column);
                }
            }

        }, SqlDefinition::getJoin, SqlDefinition::getDict, inner -> ParserUtils.asList(inner.getSubTable()));


        aliasColumns(sqlDefinition, columnAll);

        return columnAll;
    }

    /**
     * 获取所有排除字段
     */
    public static List<Column> excludeColumns(SqlDefinition sqlDefinition) {
        List<Column> excludeColumns = new ArrayList<>();
        integration(sqlDefinition, poll -> {
            List<Column> columns = poll.getExcludeColumns();
            if (columns != null) {
                excludeColumns.addAll(columns);
            }
        }, SqlDefinition::getJoin, SqlDefinition::getDict, inner -> ParserUtils.asList(inner.getSubTable()));
        return excludeColumns;
    }


    /**
     * 执行别名规则，把别名应用到查询字段上
     *
     * @param columns 查询字段
     */
    private static void aliasColumns(SqlDefinition sqlDefinition, List<Column> columns) {
        integration(sqlDefinition, poll -> {
            List<Column> alias = poll.getAlias();
            if (CollectionUtils.isEmpty(alias)) {
                return;
            }

            for (Column column : columns) {
                for (Column aliasColumn : alias) {
                    if (column.getTable().equals(aliasColumn.getTable()) &&
                            column.getColumn().equals(aliasColumn.getColumn())) {
                        column.setAlias(aliasColumn.getAlias());
                    }
                }
            }
        }, SqlDefinition::getJoin, SqlDefinition::getDict, inner -> ParamsUtils.asList(inner.getSubTable()));

    }


    /**
     * 检查字段重复
     *
     * @param sqlDefinition SQL定义信息
     */
    private static void validUniqueColumns(SqlDefinition sqlDefinition) {
        Set<String> uniqueSet = new HashSet<>();
        StringJoiner joiner = new StringJoiner(",");
        integration(sqlDefinition, poll -> {
            poll.getColumns()
                    .stream()
                    .filter(column -> !column.isDisabled())
                    .map(column -> ParamsUtils.hasText(column.getAlias(), column.getColumn()))
                    .filter(column -> !uniqueSet.add(column))
                    .forEach(joiner::add);

            List<AggregationFun> aggregationFuns = poll.getAggregationFuns();
            if (aggregationFuns == null || aggregationFuns.isEmpty()) {
                return;
            }
            aggregationFuns.stream()
                    .map(agg -> ParamsUtils.hasText(agg.getAlias(), agg.getColumn()))
                    .filter(agg -> !uniqueSet.add(agg))
                    .forEach(joiner::add);

        }, SqlDefinition::getJoin, SqlDefinition::getDict);


        Assert.isTrue(joiner.length() > 0, () -> new ParamsException("字段名重复：" + joiner));
    }


    /**
     * 获取聚合函数
     *
     * @param sqlDefinition SQL定义信息
     * @return 聚合函数集合
     */
    public static List<Column> aggregationColumns(SqlDefinition sqlDefinition) {
        List<AggregationFun> aggregationFuns = sqlDefinition.getAggregationFuns();
        if (aggregationFuns == null) {
            return Collections.emptyList();
        }
        return aggregationFuns.stream().map(item -> {
            Column column = new Column();
            column.setColumn(item.getFunPattern());
            column.setAlias(item.getAlias());
            return column;
        }).collect(Collectors.toList());
    }


    /**
     * 创建where条件sql片段
     *
     * @return sql片段
     */
    public static StringBuilder where(SqlDefinition sqlDefinition) {
        StringBuilder whereFragment = new StringBuilder(" where 1=1 ");
        integration(sqlDefinition, poll -> {
            List<ValueCondition> valueCondition = poll.getValueCondition();
            if (CollectionUtils.isEmpty(valueCondition)) {
                return;
            }
            valueCondition.sort((c1, c2) -> "and".equals(c1.getCombination().trim()) ? -1 : 1);
            for (ValueCondition w : valueCondition) {
                whereFragment.append(" ")
                        .append(ParamsUtils.hasText(w.getCombination(), "and"))
                        .append(" ")
                        .append(brackets(w));
            }
        }, SqlDefinition::getJoin);

        return whereFragment;
    }


    /**
     * 解析包含括号的查询规则
     *
     * @param valueCondition 查询条件实体类
     * @return SQL字符串
     */
    private static StringBuilder brackets(ValueCondition valueCondition) {
        List<ValueCondition> brackets = valueCondition.getBrackets();

        StringBuilder whereFragment = new StringBuilder().append(" ")
                .append(StringUtils.hasText(valueCondition.getTable()) ? valueCondition.getTable() + "." : "")
                .append(valueCondition.getColumn())
                .append(" ")
                .append(valueCondition.getCondition());
        if (CollectionUtils.isEmpty(brackets)) {
            return whereFragment;
        }
        whereFragment.insert(0, " ( ");
        for (ValueCondition bracket : brackets) {
            whereFragment.append(" ")
                    .append(bracket.getCombination())
                    .append(" ")
                    .append(brackets(bracket));
        }
        return whereFragment.append(" ) ");
    }


    /**
     * 解析join查询SQL片段
     *
     * @param sqlDefinition SQL定义信息
     * @return join查询SQL片段
     */
    public static String join(SqlDefinition sqlDefinition) {
        StringBuilder sql = new StringBuilder();
        List<SqlDefinition> joins = sqlDefinition.getJoin();
        if (CollectionUtils.isEmpty(joins)) {
            return "";
        }
        for (SqlDefinition join : joins) {
            Pair<String, String> joinOn = join.getJoinOn();
            sql.append(" ")
                    .append(join.getJoinType())
                    .append(" ")
                    .append(join.getTable())
                    .append(" on ")
                    .append(sqlDefinition.getTable())
                    .append(".")
                    .append(joinOn.getKey())
                    .append("=")
                    .append(join.getTable())
                    .append(".")
                    .append(joinOn.getValue())
                    .append(" ")
                    .append(join(join));
        }
        return sql.toString();
    }


    /**
     * 获取order by规则
     *
     * @param sqlDefinition SQL定义信息
     * @return order by 字符串
     */
    private static StringBuilder orderBy(SqlDefinition sqlDefinition) {
        StringBuilder orderBy = new StringBuilder();

        integration(sqlDefinition, poll -> {
            String orderFragment = poll.getOrderFragment();
            orderBy.append(" ")
                    .append(orderFragment == null ? "" : orderFragment)
                    .append(" ");
        }, SqlDefinition::getJoin);

        return StringUtils.hasText(orderBy) ? orderBy.insert(0, " order by ") : orderBy;
    }


    /**
     * 获取SQL的占位符数据
     *
     * @param sqlDefinition SQL定义信息
     * @return SQL占位符数据
     */
    public static Map<String, Object> getArgs(SqlDefinition sqlDefinition) {
        Map<String, Object> args = new HashMap<>();
        integration(sqlDefinition, poll -> {
            List<ValueCondition> valueConditions = poll.getValueCondition();
            if (CollectionUtils.isEmpty(valueConditions)) {
                return;
            }
            valueConditions.forEach(column -> bracketsArgs(args, column));
        }, SqlDefinition::getJoin, inner -> ParamsUtils.asList(inner.getSubTable()));
        return args;
    }


    /**
     * 递归获取包含括号的占位符数据
     *
     * @param args           占位符
     * @param valueCondition where查询数据，包含占位符数据
     */
    private static void bracketsArgs(Map<String, Object> args, ValueCondition valueCondition) {
        args.put(valueCondition.getArgName(), valueCondition.getValue());
        List<ValueCondition> brackets = valueCondition.getBrackets();
        if (CollectionUtils.isEmpty(brackets)) {
            return;
        }
        for (ValueCondition bracket : brackets) {
            bracketsArgs(args, bracket);
        }
    }


    /**
     * 获取不合法的别名
     *
     * @param sqlDefinition SQL定义信息
     * @return 不合法别名集合
     */
    public static List<Column> illegalAlias(SqlDefinition sqlDefinition) {
        List<Column> illegalAlias = new ArrayList<>();
        integration(sqlDefinition, poll -> {
            List<Column> alias = poll.getIllegalAlias();
            if (alias != null) {
                illegalAlias.addAll(alias);
            }
        }, SqlDefinition::getJoin, poll -> ParserUtils.asList(poll.getSubTable()));
        return illegalAlias;
    }


    /**
     * 获取所有表字典规则
     *
     * @param sqlDefinition SQL定义信息
     * @return 表字典规则集合
     */
    public static List<SqlDefinition> dicts(SqlDefinition sqlDefinition) {

        List<SqlDefinition> dictList = new ArrayList<>();

        integration(sqlDefinition, poll -> {
            List<SqlDefinition> dict = poll.getDict();
            if (dict != null) {
                dictList.addAll(dict);
            }
        }, poll -> ParserUtils.asList(poll.getSubTable()), SqlDefinition::getDict, SqlDefinition::getJoin);
        return dictList;
    }


    /**
     * 获取所有数据替换信息
     *
     * @param sqlDefinition SQL定义信息
     * @return 数据替换信息集合
     */
    public static List<ValueReplace> valueReplacesAll(SqlDefinition sqlDefinition) {
        List<ValueReplace> valueReplacesAll = new ArrayList<>();
        integration(sqlDefinition, poll -> {
                    List<ValueReplace> valueReplaces = poll.getValueReplaces();
                    if (valueReplaces != null) {
                        valueReplacesAll.addAll(valueReplaces);
                    }
                }, SqlDefinition::getJoin, SqlDefinition::getDict
                , inner -> ParserUtils.asList(inner.getSubTable()));

        return valueReplacesAll;
    }


    public static List<ObjectNode> expandAll(SqlDefinition sqlDefinition) {
        List<ObjectNode> expandAll = new ArrayList<>();

        integration(sqlDefinition, poll -> {
            ObjectNode expand = poll.getExpand();
            if (expand != null) {
                expandAll.add(expand);
            }
        }, poll -> ParserUtils.asList(poll.getSubTable()), SqlDefinition::getDict, SqlDefinition::getJoin);
        return expandAll;
    }

    /**
     * 深度获取指定数据
     *
     * @param sqlDefinition SQL定义信息
     * @param consumer      数据消费者，调用者使用该消费者传入的对象获取数据
     * @param inners        数据来源提供者
     */
    @SafeVarargs
    private static void integration(SqlDefinition sqlDefinition, Consumer<SqlDefinition> consumer, Function<SqlDefinition, List<SqlDefinition>>... inners) {
        if (sqlDefinition == null) {
            return;
        }
        LinkedList<SqlDefinition> linkedList = new LinkedList<>();
        linkedList.add(sqlDefinition);
        while (!linkedList.isEmpty()) {
            SqlDefinition poll = linkedList.poll();
            if (poll == null) {
                break;
            }
            consumer.accept(poll);

            for (Function<SqlDefinition, List<SqlDefinition>> inner : inners) {
                List<SqlDefinition> innerSqlDefinition = inner.apply(poll);
                if (innerSqlDefinition != null) {
                    linkedList.addAll(innerSqlDefinition);
                }
            }
        }
    }


}
