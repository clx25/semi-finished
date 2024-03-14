package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.annontation.Where;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * like查询
 * <pre>
 *     {
 *         "%col":"value",
 *         "col%":"value",
 *         "%col%":"value",
 *     }
 * </pre>
 * "%col":"value" -> where col like '%value'
 * "col%":"value" -> where col like 'value%'
 * "%col%":"value" -> where col like '%value%'
 */
@Where
@Component
@AllArgsConstructor
public class LikeParamsParser implements KeyValueParamsParser {

    private final CommonParser commonParser;
    private final TableUtils tableUtils;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        ValueCondition valueCondition = ParserUtils.columnValue(table, key);
        char[] chars = valueCondition.getColumn().toCharArray();
        char symbol = '%';
        int last = chars.length - 1;
        if (chars[0] != symbol && chars[last] != symbol) {
            return false;
        }
        value = commonParser.brackets(valueCondition, sqlDefinition.getDataSource(), key, value);
        String argsValue = value.asText();
        if (!StringUtils.hasText(argsValue)) {
            return true;
        }


        int offset = 0;
        int count = chars.length - 1;


        if (chars[offset] == symbol) {
            offset = 1;
            argsValue = symbol + argsValue;
        }
        if (chars[last] == symbol) {
            count -= offset;
            argsValue += symbol;
        }

        String column = new String(chars, offset, count);

        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column);

        tableUtils.validColumnsName(sqlDefinition, table, column);
        valueCondition.setColumn(column);

        String argName = tableUtils.uniqueAlias("like_" + table + "_" + column);
        valueCondition.setValue(argsValue);
        valueCondition.setCondition(((valueCondition.isConditionBoolean() ? "" : " not ") + " like ") + ":" + argName);
        valueCondition.setArgName(argName);
        sqlDefinition.addValueCondition(valueCondition);
        return true;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
