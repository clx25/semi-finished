package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.ParserExecutor;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.annotation.Order;
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
@Order
public class DictKeyValueParser implements SelectParamsParser {

    private final SemiCache semiCache;
    private final ParserExecutor parserExecutor;
    private final IdGenerator idGenerator;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        if (key.length() < 2 || !key.endsWith(":")) {
            return false;
        }

        Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("表字典规则错误：" + key));
        String column = key.substring(0, key.length() - 1);

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, column);

        ObjectNode node = (ObjectNode) value;

        //删除并获取on规则，在这里获取是为了校验表字典规则与on规则同时存在
        JsonNode onNode = node.remove("@on");
        Assert.isTrue(onNode == null, () -> new ParamsException("表字典规则需要配合@on规则使用：" + key));
        String on = onNode.asText();

        //解析表字典规则
        SqlDefinition dict = parserExecutor.parse(node);

        TableUtils.validColumnsName(semiCache, dict, dict.getTable(), on);


        String mainOn = complete(sqlDefinition, dict, column, TableUtils.uniqueAlias(idGenerator, "dict_" + table + "_" + column));
        String innerOn = complete(dict, dict, on, TableUtils.uniqueAlias(idGenerator, "dict_" + dict.getTable() + "_" + on));

        dict.setJoinOn(new Pair<>(mainOn, innerOn));
        sqlDefinition.addDict(dict);


        return false;
    }

    /**
     * 判断查询字段包不包含关联字段，如果不包含，那么添加该字段，并添加到排除列表，在匹配后删除并返回创建的别名
     * 如果包含，那么有别名就返回别名，没有别名就返回字段名
     *
     * @param sqlDefinition 主查询SQL定义信息
     * @param dict          字典表查询定义信息
     * @param column
     * @param alias
     * @return
     */
    private String complete(SqlDefinition sqlDefinition, SqlDefinition dict, String column, String alias) {
        String table = sqlDefinition.getTable();
        return sqlDefinition.getColumns().stream()
                .filter(col -> table.equals(col.getTable()) && column.equals(col.getColumn()))
                .findFirst()
                .map(col -> ParamsUtils.hasText(col.getAlias(), column))
                .orElseGet(() -> {
                    dict.addExcludeColumns("", alias);
                    sqlDefinition.addColumn(table, column, alias);
                    return alias;
                });
    }
}
