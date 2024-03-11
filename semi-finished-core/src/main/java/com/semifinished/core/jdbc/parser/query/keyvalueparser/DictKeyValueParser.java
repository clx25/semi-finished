package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.ParserExecutor;
import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

/**
 * 表字典规则解析
 * <pre>
 *     {
 *         "col1:":{
 *             "@tb":"table",
 *             "@on":"col2",
 *             "@":"col3,col4"
 *         }
 *     }
 * </pre>
 */
@Component
@AllArgsConstructor
public class DictKeyValueParser implements KeyValueParamsParser {

    private final ParserExecutor parserExecutor;
    private final CommonParser commonParser;
    private final TableUtils tableUtils;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        if (key.length() < 2 || !key.trim().endsWith(":")) {
            return false;
        }

        boolean statusAnyMatch = ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL,
                ParserStatus.DICTIONARY, ParserStatus.SUB_TABLE);

        Assert.isFalse(statusAnyMatch, () -> new ParamsException("表字典规则位置错误"));

        if (value instanceof ArrayNode) {
            for (JsonNode jsonNode : value) {
                parseDict(table, key, jsonNode, sqlDefinition);
            }
            return true;
        }

        parseDict(table, key, value, sqlDefinition);


        return true;
    }

    private void parseDict(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("表字典规则错误：" + key));

        String column = key.substring(0, key.length() - 1);
        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column);
        tableUtils.validColumnsName(sqlDefinition, table, column);

        ObjectNode node = (ObjectNode) value;

        //删除并获取on规则，在这里获取是为了校验表字典规则与on规则同时存在
        JsonNode onNode = node.remove("@on");
        Assert.isTrue(onNode == null, () -> new ParamsException("表字典规则需要配合@on规则使用：" + key));
        String on = onNode.asText().trim();

        //解析表字典规则
        SqlDefinition dict = new SqlDefinition(node);
        dict.setDataSource(sqlDefinition.getDataSource());
        dict.setStatus(ParserStatus.DICTIONARY.getStatus());
        parserExecutor.parse(dict);
        on = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, on);
        tableUtils.validColumnsName(dict, dict.getTable(), on);


        String mainOn = complete(sqlDefinition, dict, column, tableUtils.uniqueAlias("dict_" + table + "_" + column));
        String innerOn = complete(dict, dict, on, tableUtils.uniqueAlias("dict_" + dict.getTable() + "_" + on));

        dict.setJoinOn(new Pair<>(mainOn, innerOn));
        sqlDefinition.addDict(dict);
    }


    /**
     * 判断查询字段包不包含关联字段，如果不包含，那么添加该字段，并添加到排除列表，在匹配后删除并返回创建的别名
     * 如果包含，那么有别名就返回别名，没有别名就返回字段名
     *
     * @param sqlDefinition SQL定义信息
     * @param dict          字典表查询定义信息
     * @param column
     * @param alias
     * @return
     */
    private String complete(SqlDefinition sqlDefinition, SqlDefinition dict, String column, String alias) {
        String table = sqlDefinition.getTable();
        return QuerySqlCombiner.queryColumns(sqlDefinition).stream()
                .filter(col -> table.equals(col.getTable()) && column.equals(col.getColumn()))
                .findFirst()
                .map(col -> ParamsUtils.hasText(col.getAlias(), column))
                .orElseGet(() -> {
                    dict.addExcludeColumns("", alias);
                    sqlDefinition.addColumn(table, column, alias);
                    return alias;
                });
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
