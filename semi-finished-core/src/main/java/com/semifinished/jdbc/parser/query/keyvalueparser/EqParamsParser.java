package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.annontation.Where;
import com.semifinished.cache.SemiCache;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.ParamsUtils;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.TableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 解析等于规则
 * <pre>
 *     {
 *         "col":"value"
 *     }
 * </pre>
 * 以上解析为 where col=value
 */

@Slf4j
@Where
@Component
@RequiredArgsConstructor
public class EqParamsParser implements SelectParamsParser{

    private final SemiCache semiCache;
    private final CommonParser commonParser;
    private final IdGenerator idGenerator;

    /**
     * 当前解析器为最后一个解析器，所以只返回true
     * 所有剩余的参数都会被该解析器解析，如果解析出的参数不符合规则，就会抛出异常
     *
     * @return true
     */
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        ValueCondition valueCondition = ParserUtils.columnValue(table, key);
        key = valueCondition.getColumn();
        if (key.endsWith("=")) {
            key = key.substring(0, key.length() - 1);
        }
        if (!ParamsUtils.isLegalName(key)) {
            return true;
        }

        value = commonParser.brackets(valueCondition, key, value);

        key = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, key);

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, key);
        boolean conditionBoolean = valueCondition.isConditionBoolean();
        String argName = TableUtils.uniqueAlias(idGenerator, "eq_" + table + "_" + key);
        if ("id".equals(key)) {
            argName = "id";
        }
        valueCondition.setColumn(key);
        valueCondition.setArgName(argName);

        sqlDefinition.addColumnValue(valueCondition);
        if (value.isNull()) {
            if (log.isDebugEnabled()) {
                log.debug("表" + table + "添加查询条件" + key + " is " + (conditionBoolean ? "" : "not ") + "null");
            }
            valueCondition.setCondition(" is " + (conditionBoolean ? "" : "not "));
            return true;
        }
        valueCondition.setCondition((conditionBoolean ? "" : "!") + "=:" + argName);

        valueCondition.setValue(value.isBoolean() ? value.asBoolean() : value.asText());
        if (log.isDebugEnabled()) {
            log.debug("表" + table + "添加查询条件" + key + (conditionBoolean ? "" : "!") + "=" + value.asText());
        }


        return true;
    }

    @Override
    public int getOrder() {
        return 2000;
    }
}
