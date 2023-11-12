package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.annontation.Where;
import com.semifinished.cache.SemiCache;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.jdbc.util.IdGenerator;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
public class LikeParamsParser implements SelectParamsParser{
    private final SemiCache semiCache;
    private final IdGenerator idGenerator;
    private final CommonParser commonParser;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        ValueCondition valueCondition = ParserUtils.columnValue(table, key);
        char[] chars = valueCondition.getColumn().toCharArray();
        char symbol = '%';
        int last = chars.length - 1;
        if (chars[0] != symbol && chars[last] != symbol) {
            return false;
        }
        value = commonParser.brackets(valueCondition, key, value);


        int offset = 0;
        int count = chars.length - 1;

        String argsValue = value.asText();

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

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, column);
        valueCondition.setColumn(column);

        String argName = TableUtils.uniqueAlias(idGenerator, "like_" + table + "_" + column);
        valueCondition.setValue(argsValue);
        valueCondition.setCondition(((valueCondition.isConditionBoolean() ? "" : " not ") + " like ") + ":" + argName);
        valueCondition.setArgName(argName);
        sqlDefinition.addColumnValue(valueCondition);
        return true;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
