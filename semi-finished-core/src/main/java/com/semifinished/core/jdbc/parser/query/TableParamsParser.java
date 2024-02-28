package com.semifinished.core.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.ParserExecutor;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
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

    private final ParserExecutor parserExecutor;
    private final CommonParser commonParser;
    private final TableUtils tableUtils;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {

        JsonNode tbNode = params.remove("@tb");

        if (!ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE, ParserStatus.JOIN, ParserStatus.DICTIONARY)) {
            Assert.isTrue(tbNode != null, () -> new ParamsException("表名规则位置错误"));
            return;
        }
        if (tbNode == null && sqlDefinition.getStatus() == ParserStatus.DICTIONARY.getStatus()) {
            return;
        }
        Assert.isTrue(tbNode == null || (tbNode instanceof ValueNode && !StringUtils.hasText(tbNode.asText())), () -> new ParamsException("未指定表名"));

        if (tbNode instanceof ValueNode) {
            String tb = tbNode.asText();
            tb = commonParser.getActualTable(sqlDefinition.getDataSource(), tb);
            tableUtils.validColumnsName(sqlDefinition, tb);
            sqlDefinition.setTable(tb);
            return;
        }

        //解析子查询规则
        SqlDefinition subSqlDefinition = new SqlDefinition(tbNode.deepCopy());
        subSqlDefinition.setStatus(ParserStatus.SUB_TABLE.getStatus());
        subSqlDefinition.setDataSource(sqlDefinition.getDataSource());
        parserExecutor.parse(subSqlDefinition);
        sqlDefinition.setTable(tableUtils.uniqueAlias(subSqlDefinition.getTable()));
        sqlDefinition.setSubTable(subSqlDefinition);
    }

    @Override
    public int getOrder() {
        return -900;
    }
}
