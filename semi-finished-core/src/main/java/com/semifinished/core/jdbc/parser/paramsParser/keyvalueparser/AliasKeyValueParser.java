package com.semifinished.core.jdbc.parser.paramsParser.keyvalueparser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.CommonParser;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 在不指定或排除字段的情况下，设置别名
 * <pre>
 *     {
 *         ":":"col:alias,col2:alias2"
 *     }
 * </pre>
 */
@Component
@AllArgsConstructor
public class AliasKeyValueParser implements KeyValueParamsParser {

    private final TableUtils tableUtils;

    private final CommonParser commonParser;

    /**
     * 解析别名规则
     * 当别名不符合规则时，为了避免SQL注入，采用修改返回数据的方式实现别名
     */
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!":".equals(key.trim())) {
            return false;
        }

        Assert.isTrue(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE, ParserStatus.JOIN, ParserStatus.DICTIONARY), () -> new ParamsException("别名规则位置错误"));


        String[] values;
        if (value instanceof ArrayNode) {
            values = new String[value.size()];
            for (int i = 0; i < value.size(); i++) {
                values[i] = value.get(i).asText(null);
                Assert.notBlank(values[i], () -> new ParamsException("别名不能为空：" + key));
            }
        } else {
            values = value.asText().split(",");
        }


        for (int i = 0; i < values.length; i++) {

            String[] alias = values[i].split(":");
            Assert.isFalse(alias.length != 2, () -> new ParamsException("别名参数错误"));

            values[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, alias[0]);
            //todo 已知问题，当使用了子查询，且别名为不合法别名时，外层查询无法匹配到对应字段
            if (!ParamsUtils.isLegalName(alias[1])) {
                String legalAlias = tableUtils.uniqueAlias("legal_" + table + "_" + alias[0]);
                sqlDefinition.addIllegalAlias(table, legalAlias, alias[1]);
                alias[1] = legalAlias;
            }
            sqlDefinition.addAlias(table, values[i], alias[1]);
        }

        tableUtils.validColumnsName(sqlDefinition, table, values);
        return true;
    }

    @Override
    public int getOrder() {
        return -800;
    }
}
