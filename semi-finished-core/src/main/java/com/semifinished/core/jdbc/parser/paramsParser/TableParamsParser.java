package com.semifinished.core.jdbc.parser.paramsParser;

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

import java.util.Iterator;
import java.util.Map;

/**
 * 解析查询的表名
 * <pre>
 *     {
 *         "@tb":"tb1"
 *     }
 * </pre>
 * select column  from tb1
 * todo 返回的字段转驼峰命名法
 */
@Component
@AllArgsConstructor
public class TableParamsParser implements ParamsParser {

    private final ParserExecutor parserExecutor;
    private final CommonParser commonParser;
    private final TableUtils tableUtils;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        String field = null;
        JsonNode tbNode = null;
        Iterator<Map.Entry<String, JsonNode>> iterator = params.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            String key = entry.getKey();
            if (key.startsWith("@tb")) {
                Assert.hasText(field, () -> new ParamsException("指定表规则重复"));
                field = key;
                tbNode = entry.getValue();
                iterator.remove();
            }
        }
        if (field == null) {
            return;
        }

        if (field.equals("@tb1")) {
            field = field.substring(4);
            sqlDefinition.setDistinct(true);
        }

        if (field.startsWith("@tb@")) {
            String column = field.substring(1);
            tableUtils.validColumnsName(sqlDefinition, field, column);
            //todo 指定返回的字段

            sqlDefinition.addColumn(tbNode.asText(""), column, "semi__id__key");
            sqlDefinition.addExcludeColumns(tbNode.asText(""), "semi__id__key");

            sqlDefinition.setResultId(true);
        }


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
