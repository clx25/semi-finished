package com.semifinished.jdbc;


import com.semifinished.exception.ParamsException;
import com.semifinished.pojo.AggregationFun;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.math3.util.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 把{@link SqlDefinition}中的数据组合成sql的接口
 */

@Getter
@Builder
@AllArgsConstructor
public class SqlCombiner {


    /**
     * 把{@link SqlDefinition}中的数据组合成sql
     *
     * @return 组装完整的sql, 这个sql会直接进行执行
     */
    public static String select(SqlDefinition sqlDefinition) {
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

    private static StringBuilder groupBy(SqlDefinition sqlDefinition) {

        StringBuilder sql = new StringBuilder();

        List<Column> groupByList = sqlDefinition.getGroupBy();
        if (groupByList != null && !groupByList.isEmpty()) {
            String groupBy = groupByList.stream()
                    .map(g -> g.getTable() + "." + g.getColumn())
                    .collect(Collectors.joining(","));
            sql.append(" group by ").append(groupBy).append(" ");
        }
        return sql;
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
            return " ( " + select(sqlDefinition) + " ) " + sqlDefinition.getTable();
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
        List<Column> columnAll = columnsAll(sqlDefinition);
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
        List<Column> groupBy = sqlDefinition.getGroupBy();
        List<Column> columns = columnsAll(sqlDefinition);
        List<Column> aggregationColumns = aggregationColumns(sqlDefinition);
        return (sqlDefinition.isDistinct() ? " distinct " : "") + Stream.concat(columns.stream()
                        .filter(col -> !sqlDefinition.isToMany() || groupBy.stream()
                                .anyMatch(group -> col.getTable().equals(group.getTable())
                                        && col.getColumn().equals(group.getColumn())
                                )
                        ), aggregationColumns.stream()).map(col -> (StringUtils.hasText(col.getTable()) ? (col.getTable() + ".") : "") + col.getColumn() + " " + getAlias(col.getAlias()))
                .collect(Collectors.joining(","));
    }

    /**
     * 获取所有普通查询字段，不包含聚合字段
     *
     * @param sqlDefinition SQL定义信息
     * @return 所有普通查询字段，不包含聚合字段
     */
    public static List<Column> columnsAll(SqlDefinition sqlDefinition) {

        List<Column> columnAll = new ArrayList<>(sqlDefinition.getColumns());

        LinkedList<SqlDefinition> linkedList = new LinkedList<>();
        List<SqlDefinition> join = sqlDefinition.getJoin();

        if (join != null) {
            linkedList.addAll(join);
        }

        while (!linkedList.isEmpty()) {
            SqlDefinition poll = linkedList.poll();
            if (poll == null) {
                break;
            }
            columnAll.addAll(poll.getColumns());
            columnAll.addAll(aggregationColumns(poll));
            List<SqlDefinition> innerJoin = poll.getJoin();
            if (innerJoin != null) {
                linkedList.addAll(innerJoin);
            }
        }

        excludeColumns(sqlDefinition, columnAll);

        aliasColumns(sqlDefinition, columnAll);

        validUniqueColumns(columnAll);

        return columnAll;
    }


    /**
     * 执行字段排除规则，把不查询的字段排除
     *
     * @param columns 所有查询的字段
     */
    private static void excludeColumns(SqlDefinition sqlDefinition, List<Column> columns) {
        integration(sqlDefinition, join -> {
            List<Column> excludeColumns = join.getExcludeColumns();
            if (excludeColumns == null || excludeColumns.isEmpty()) {
                return;
            }

            columns.removeIf(column -> excludeColumns.stream()
                    .filter(col -> col.getTable().equals(column.getTable()))
                    .anyMatch(col -> col.getColumn().equals(column.getColumn())));
        });
    }

    public static List<Column> excludeRecordsColumns(SqlDefinition sqlDefinition) {
        List<Column> excludeRecordsColumns = new ArrayList<>();
        List<Column> recordsColumns = sqlDefinition.getExcludeRecordsColumns();
        if (recordsColumns != null) {
            excludeRecordsColumns.addAll(recordsColumns);
        }
        integration(sqlDefinition, join -> {
            List<Column> excludeColumns = join.getExcludeRecordsColumns();
            if (excludeColumns != null) {
                excludeRecordsColumns.addAll(excludeColumns);
            }

        });
        List<SqlDefinition> dictList = sqlDefinition.getDict();
        if (dictList == null) {
            return excludeRecordsColumns;
        }
        for (SqlDefinition dict : dictList) {
            List<Column> excludeColumns = dict.getExcludeRecordsColumns();
            if (excludeColumns != null) {
                excludeRecordsColumns.addAll(excludeColumns);
            }
        }
        return excludeRecordsColumns;
    }

    /**
     * 执行别名规则，把别名应用到查询字段上
     *
     * @param columns 查询字段
     */
    private static void aliasColumns(SqlDefinition sqlDefinition, List<Column> columns) {
        integration(sqlDefinition, join -> {
            List<Column> alias = join.getAlias();
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
        });


    }

    /**
     * 检查字段重复
     *
     * @param columns 所有查询字段
     */
    private static void validUniqueColumns(List<Column> columns) {
        Set<String> uniqueSet = new HashSet<>();
        StringJoiner joiner = new StringJoiner(",");
        for (Column column : columns) {
            String alias = column.getAlias();
            String col = ParamsUtils.hasText(alias, column.getColumn());
            if (!uniqueSet.add(col)) {
                joiner.add(col);
            }
        }
        Assert.isTrue(joiner.length() > 0, () -> new ParamsException("字段名重复:" + joiner));
    }

    /**
     * 获取聚合函数
     *
     * @param sqlDefinition SQL定义信息
     * @return 聚合函数集合
     */
    private static List<Column> aggregationColumns(SqlDefinition sqlDefinition) {
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

    public static String getAlias(String alias) {
        return StringUtils.hasText(alias) ? "'" + alias + "'" : "";
    }

    /**
     * 创建where条件sql片段
     *
     * @return sql片段
     */
    public static StringBuilder where(SqlDefinition sqlDefinition) {
        StringBuilder whereFragment = new StringBuilder(" where 1=1 ");
        integration(sqlDefinition, join -> {
            List<ValueCondition> valueCondition = join.getValueCondition();
            if (CollectionUtils.isEmpty(valueCondition)) {
                return;
            }
            valueCondition.sort((c1, c2) -> "and".equals(c1.getCombination()) ? -1 : 1);
            for (ValueCondition w : valueCondition) {
                whereFragment.append(" ")
                        .append(ParamsUtils.hasText(w.getCombination(), "and"))
                        .append(" ")
                        .append(brackets(w));
            }
        });

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
     * @return order by 字符串
     */
    private static StringBuilder orderBy(SqlDefinition sqlDefinition) {
        StringBuilder orderBy = new StringBuilder();

        integration(sqlDefinition, poll -> {
            String orderFragment = poll.getOrderFragment();
            orderBy.append(" ")
                    .append(orderFragment == null ? "" : orderFragment)
                    .append(" ");
        });

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
        subTableArgs(args, sqlDefinition);
        integration(sqlDefinition, join -> {
            subTableArgs(args, join);
            List<ValueCondition> valueConditions = join.getValueCondition();
            if (CollectionUtils.isEmpty(valueConditions)) {
                return;
            }
            valueConditions.forEach(column -> bracketsArgs(args, column));
        });
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


    private static void subTableArgs(Map<String, Object> args, SqlDefinition sqlDefinition) {
        SqlDefinition subTable = sqlDefinition.getSubTable();
        while (subTable != null) {
            List<ValueCondition> valueCondition = subTable.getValueCondition();
            if (CollectionUtils.isEmpty(valueCondition)) {
                return;
            }
            valueCondition.forEach(column -> args.put(column.getArgName(), column.getValue()));
            subTable = subTable.getSubTable();
        }
    }

    private static void integration(SqlDefinition sqlDefinition, Consumer<SqlDefinition> consumer) {
        LinkedList<SqlDefinition> linkedList = new LinkedList<>();
        linkedList.add(sqlDefinition);
        while (!linkedList.isEmpty()) {
            SqlDefinition poll = linkedList.poll();
            if (poll == null) {
                break;
            }
            consumer.accept(poll);

            List<SqlDefinition> innerJoin = poll.getJoin();
            if (innerJoin != null) {
                linkedList.addAll(innerJoin);
            }
        }
    }


    public static String insert(SqlDefinition sqlDefinition) {
        List<ValueCondition> valueCondition = sqlDefinition.getValueCondition();
        Assert.isEmpty(valueCondition, () -> new ParamsException("新增字段不能为空"));
        StringJoiner cols = new StringJoiner(",");
        StringJoiner values = new StringJoiner(",");
        String table = sqlDefinition.getTable();
        for (ValueCondition column : valueCondition) {
            String col = column.getColumn();
            if ("id".equalsIgnoreCase(col)) {
                continue;
            }
            cols.add(table + "." + col);
            values.add(":" + column.getArgName());
        }

        return "insert " + table + " (" + cols + ") values(" + values + ")";
    }

    public static String update(SqlDefinition sqlDefinition) {
        List<ValueCondition> valueCondition = sqlDefinition.getValueCondition();
        Assert.isEmpty(valueCondition, () -> new ParamsException("修改字段不能为空"));

        StringJoiner cols = new StringJoiner(",");
        long count = valueCondition.stream().filter(col -> !"id".equals(col.getColumn()))
                .peek(col -> cols.add(col.getColumn() + "=:" + col.getArgName())
                ).count();

        Assert.isTrue(count == valueCondition.size(), () -> new ParamsException("更新缺少id"));

        return "update " + sqlDefinition.getTable() + " set " + cols + " where id=:id";
    }

    public static String delete(SqlDefinition sqlDefinition) {
        return "delete from " + sqlDefinition.getTable() + where(sqlDefinition);
    }
}
