package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.TableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
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
    private final SemiCache semiCache;
    private final IdGenerator idGenerator;
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
        String[] values = value.asText().split(",");
        for (int i = 0; i < values.length; i++) {
            String[] alias = values[i].split(":");
            Assert.isTrue(alias.length != 2, () -> new ParamsException("别名参数错误"));

            values[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, alias[0]);

            if (!ParamsUtils.isLegalName(alias[1])) {
                String legalAlias = TableUtils.uniqueAlias(idGenerator, "legal_" + table + "_" + alias[0]);
                sqlDefinition.addIllegalAlias(null, legalAlias, alias[1]);
                alias[1] = legalAlias;
            }
            sqlDefinition.addAlias(table, values[i], alias[1]);
        }

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, values);
        return true;
    }

    @Override
    public int getOrder() {
        return -800;
    }
}
