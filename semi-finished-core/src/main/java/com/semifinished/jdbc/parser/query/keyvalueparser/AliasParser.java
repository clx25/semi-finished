package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.constant.ParserStatus;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.bean.TableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 在不指定或排除字段的情况下，设置别名
 * <pre>
 *     {
 *         ":":"col:alias,col2:alias2"
 *     }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class AliasParser implements SelectParamsParser {

    private final TableUtils tableUtils;
    @Resource
    private CommonParser commonParser;

    /**
     * 解析别名规则
     * 当别名不符合规则时，为了避免SQL注入，采用修改返回数据的方式实现别名
     */
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!":".equals(key)) {
            return false;
        }

        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE,
                ParserStatus.JOIN, ParserStatus.DICTIONARY), () -> new ParamsException("别名规则位置错误"));

        String[] values = value.asText().split(",");
        for (int i = 0; i < values.length; i++) {
            String[] alias = values[i].split(":");
            Assert.isTrue(alias.length != 2, () -> new ParamsException("别名参数错误"));

            values[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, alias[0]);

            if (!ParamsUtils.isLegalName(alias[1])) {
                String legalAlias = tableUtils.uniqueAlias("legal_" + table + "_" + alias[0]);
                sqlDefinition.addIllegalAlias(null, legalAlias, alias[1]);
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
