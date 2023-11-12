package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.ParserExecutor;
import com.semifinished.util.Assert;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 解析查询的表名
 * <pre>
 *     {
 *         "@tb":"tb1"
 *     }
 * </pre>
 * select column  from tb1
 */
@Component
@AllArgsConstructor
public class TableParamsParser implements ParamsParser {
    private final SemiCache semiCache;
    private final ParserExecutor parserExecutor;
    private final CommonParser commonParser;

    @Override
    public void parser(ObjectNode params, SqlDefinition sqlDefinition) {
        JsonNode tbNode = params.remove("@tb");
        Assert.isTrue(tbNode == null || !StringUtils.hasText(tbNode.asText()), () -> new ParamsException("没有指定表名"));

        if (tbNode instanceof ValueNode) {
            String tb = tbNode.asText();
            tb = commonParser.getActualTable(sqlDefinition.getDataSource(), tb);
            TableUtils.validColumnsName(semiCache, sqlDefinition, tb);
            sqlDefinition.setTable(tb);
            return;
        }

        params = tbNode.deepCopy();

        SqlDefinition innerSqlDefinition = parserExecutor.parse(params);
        sqlDefinition.setTable("__semi_inner_table__" + innerSqlDefinition.getTable());
        sqlDefinition.setSubTable(innerSqlDefinition);
    }

    @Override
    public int getOrder() {
        return -900;
    }
}
