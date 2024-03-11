package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.annontation.Where;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class EqParamsParser implements KeyValueParamsParser {

    private final CommonParser commonParser;
    private final TableUtils tableUtils;

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
            return false;
        }

        value = commonParser.brackets(valueCondition, sqlDefinition.getDataSource(), key, value);

        key = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, key);

        tableUtils.validColumnsName(sqlDefinition, table, key);
        boolean conditionBoolean = valueCondition.isConditionBoolean();
        String argName = tableUtils.uniqueAlias("eq_" + table + "_" + key);
        valueCondition.setColumn(key);
        valueCondition.setArgName(argName);

        sqlDefinition.addValueCondition(valueCondition);
        if (value.isNull()) {
            if (log.isDebugEnabled()) {
                log.debug("表" + table + "添加查询条件" + key + " is " + (conditionBoolean ? "" : "not ") + "null");
            }
            valueCondition.setCondition(" is " + (conditionBoolean ? "" : "not ") + "null ");
            return true;
        }
        valueCondition.setCondition((conditionBoolean ? "" : "!") + "=:" + argName);

        valueCondition.setValue(value.isBoolean() ? value.asBoolean() : value.asText(null));
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
