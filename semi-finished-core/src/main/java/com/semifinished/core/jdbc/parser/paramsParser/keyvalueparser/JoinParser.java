package com.semifinished.core.jdbc.parser.paramsParser.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.ParserExecutor;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * join查询
 * 该规则配合join on规则使用
 *
 * @see JoinOnKeyValueParser
 * <pre>
 *     "@table":"tb1",
 *     "&col1":{
 *         "@tb":"tb2",
 *         "@on":"col2"
 *     }
 * </pre>
 * 以上解析为tb1 left join tb2 on tb1.col1=tb2.col2
 */
@Component
@RequiredArgsConstructor
public class JoinParser implements KeyValueParamsParser {

    private final TableUtils tableUtils;
    @Resource
    private ParserExecutor parserExecutor;


    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {


        boolean left = key.startsWith("&");
        boolean inner = key.endsWith("&");
        if (!left && !inner) {
            return false;
        }
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE,
                ParserStatus.JOIN, ParserStatus.DICTIONARY), () -> new ParamsException("join规则位置错误"));


        Assert.isTrue(left && inner, () -> new ParamsException("join规则错误：" + key));
        Assert.isFalse(value.isObject(), () -> new ParamsException("join规则错误"));

        String col = left ? key.substring(1) : key.substring(0, key.length() - 1);
        Assert.isFalse(StringUtils.hasText(col), () -> new ParamsException("join规则字段名不能为空：" + key));

        ObjectNode node = (ObjectNode) value;

        JsonNode onNode = node.remove("@on");
        Assert.isTrue(onNode == null, () -> new ParamsException("join规则需要配合@on规则使用：" + key));
        String on = onNode.asText();

        SqlDefinition join = new SqlDefinition(node);
        join.setDataSource(sqlDefinition.getDataSource());
        join.setJoinType(inner ? " inner join " : " left join ");
        join.setStatus(ParserStatus.JOIN.getStatus());
        parserExecutor.parse(node, join);

        tableUtils.validColumnsName(sqlDefinition, table, col);
        tableUtils.validColumnsName(join, join.getTable(), on);

        join.setJoinOn(new Pair<>(col, on));

        sqlDefinition.addJoin(join);
        return true;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
