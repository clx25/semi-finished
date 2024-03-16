package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 指定查询字段
 * \@：指定查询字段
 * \@1:指定查询字段，并设置去重
 * <pre>
 *
 *     {
 *         "@1":"col:alias,col2:alias2",
 *     }
 * </pre>
 */

@Component
@AllArgsConstructor
public class DetermineColumnsParser implements ParamsParser {
    private final CommonParser commonParser;
    private final TableUtils tableUtils;
    private final String[] patterns = {"min(", "max(", "avg(", "sum(", "count("};

    /**
     * 解析查询字段规则
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {


        JsonNode columnsNode = params.remove("@");

        if (!ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE, ParserStatus.JOIN, ParserStatus.DICTIONARY)) {
            Assert.isTrue(columnsNode != null, () -> new ParamsException("列名规则位置错误"));
            return;
        }


        String table = sqlDefinition.getTable();

        //如果没有指定字段，默认所有字段
        if (columnsNode == null || columnsNode instanceof NullNode) {
            allColumns(table, sqlDefinition);
            return;
        }

        String column = columnsNode.asText().trim();
        if (!StringUtils.hasText(column)) {
            return;
        }
        String[] columns = column.split(",");

        //用于保存需要校验的字段
        List<String> validColumns = new ArrayList<>();

        for (String col : columns) {
            Assert.isFalse(StringUtils.hasText(col), () -> new ParamsException("查询字段错误，字段名不能为空：" + column));

            String[] alias = col.split(":");
            Assert.isFalse(StringUtils.hasText(alias[0]) && alias.length < 3, () -> new ParamsException("查询字段错误：" + column));

            String actualColumn = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, alias[0].trim());

            boolean hasAlias = alias.length == 2;

            //如果没有别名，并且设置了字段映射，那么使用映射的字段作为别名

            String a = alias[0];
            //创建一个长度为2的数组，并拷贝旧数组
            alias = Arrays.copyOf(alias, 2);
            alias[0] = actualColumn;
            alias[1] = (hasAlias ? alias[1] : a).trim();


            //不合法的别名就不放到SQL查询，而是查询后对结果进行处理
            if (hasAlias && !ParamsUtils.isLegalName(alias[1])) {
                String legalAlias = tableUtils.uniqueAlias("_alias");
                sqlDefinition.addIllegalAlias(null, legalAlias, alias[1]);
                alias[1] = legalAlias;
            }

            //根据是普通字段还是聚合函数进行处理，并返回字段名称
            col = columnOrAggregationFun(table, alias, sqlDefinition);
            if (StringUtils.hasText(col)) {
                validColumns.add(col);
            }
        }
        tableUtils.validColumnsName(sqlDefinition, table, validColumns);
    }

    /**
     * 根据是普通字段还是聚合函数进行处理,并返回字段名称
     *
     * @param table         表名
     * @param alias         字段名和别名
     * @param sqlDefinition SQL定义信息
     */
    private String columnOrAggregationFun(String table, String[] alias, SqlDefinition sqlDefinition) {
        String col = alias[0];
        for (String pattern : patterns) {
            if (!(col.startsWith(pattern) && col.endsWith(")"))) {
                continue;
            }
            col = col.substring(pattern.length(), col.length() - 1);
            col = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, col);

            boolean asterisk = "*".equals(col);

            sqlDefinition.addAggregationFun(table, col, pattern + (asterisk ? col : table + "." + col) + ")", alias.length == 2 ? alias[1] : "");

            return asterisk ? null : col;
        }
//        col = commonParser.getActualColumn(table, col);
        sqlDefinition.addColumn(table, col, alias.length == 2 ? alias[1] : "");
        return col;
    }

    /**
     * 添加表的所有字段为查询字段
     *
     * @param table         表名
     * @param sqlDefinition SQL定义信息
     */
    public void allColumns(String table, SqlDefinition sqlDefinition) {
        List<Column> columns = tableUtils.getColumns(sqlDefinition.getDataSource(), table);
        if (columns.isEmpty()) {
            //如果是子查询，外层查询的字段就是内层返回的字段
            SqlDefinition subTable = sqlDefinition.getSubTable();
            if (subTable != null) {
                columns = QuerySqlCombiner.queryColumns(subTable);
                Assert.isEmpty(columns, () -> new ParamsException("请求字段为空"));
            }
        }
        Assert.isEmpty(columns, () -> new ParamsException(table + "参数错误"));

        columns.stream()
                .map(column -> ParamsUtils.hasText(column.getAlias(), column.getColumn()))
                .forEach(column -> sqlDefinition.addColumn(table, column, commonParser.getActualAlias(sqlDefinition.getDataSource(), table, column)));
    }


    @Override
    public int getOrder() {
        return -100;
    }
}
