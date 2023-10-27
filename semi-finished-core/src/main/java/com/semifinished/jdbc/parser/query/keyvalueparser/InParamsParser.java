package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.semifinished.annontation.Where;
import com.semifinished.cache.SemiCache;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.DataAccessParser;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * in查询
 * <pre>
 *     {
 *         "[col]":"v1,v2",
 *         "[col]":["v1","v2"]
 *     }
 * </pre>
 * 以上为规则的两个写法，都解析为 where col in ('v1','v2')
 */
@Order(-100)
@Slf4j
@Where
@Component
@AllArgsConstructor
public class InParamsParser implements SelectParamsParser, DataAccessParser {
    private final SemiCache semiCache;
    private final CommonParser commonParser;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        ValueCondition valueCondition = ParserUtils.columnValue(table, key);
        char[] chars = valueCondition.getColumn().toCharArray();
        int end = chars.length - 1;
        if (chars[0] != '[' || chars[end] != ']') {
            return false;
        }

        value = commonParser.brackets(valueCondition, key, value);

        String[] values;
        if (value instanceof ArrayNode) {
            List<String> nodes = new ArrayList<>();
            value.elements().forEachRemaining(node -> {
                nodes.add(node.asText());
            });
            values = nodes.toArray(new String[0]);
        } else {
            values = value.asText().split(",");
        }

        String column = new String(chars, 1, end - 1);

        column = commonParser.getActualColumn(table, column);

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, column);
        valueCondition.setColumn(column);
        String argName = "in_" + table + "_" + column;
        boolean conditionBoolean = valueCondition.isConditionBoolean();
        valueCondition.setCondition((conditionBoolean ? "" : "not ") + "in( :" + argName + ")");
        valueCondition.setArgName(argName);
        valueCondition.setValue(Arrays.asList(values));
        sqlDefinition.addColumnValue(valueCondition);
        if (log.isDebugEnabled()) {
            log.debug("表" + table + "添加查询条件" + key + (conditionBoolean ? "" : "not ") + "in(", argName + ")");
        }
        return true;
    }

}
