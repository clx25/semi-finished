package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.annontation.Where;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.SelectParamsParser;
import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 范围查询
 * <pre>
 *     {
 *          ">key":"value",
 *          "<key":"value",
 *          ">key>":"value1,value2",
 *          "<key<":"value1,value2"
 *     }
 * </pre>
 * ">key":"value" -> value>key
 * "<key":"value" -> value<key"
 * ">key>":"value1,value2" -> value1>key>value2
 * "<key<":"value1,value2" -> value1<key<value2
 */

@Where
@Component
@AllArgsConstructor
public class RangeParamsParser implements SelectParamsParser {

    private final TableUtils tableUtils;
    private final CommonParser commonParser;

    /**
     * 范围查询
     *
     * @param key           前端的key
     * @param value         前端的value
     * @param sqlDefinition SQL定义信息
     * @return 是否匹配到范围查询
     */
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        ValueCondition valueCondition = ParserUtils.columnValue(table, key);

        char[] chars = valueCondition.getColumn().toCharArray();

        char operator = 0;
        int length = chars.length;

        int offset = 0;//字段之前的符号长度
        int lastIndex;//字段之后的符号位置
        int count = length;//获取字段时的截取长度

        if (has(chars)) {
            value = commonParser.brackets(valueCondition, sqlDefinition.getDataSource(), key, value);
            offset = chars[1] == '=' ? 2 : 1;
            operator = (char) (62 ^ 60 ^ chars[0]);
            count -= offset;
            if ((lastIndex = hasLast(chars)) > 0) {
                String text = value.asText(null);
                Assert.hasNotText(text, () -> new ParamsException("范围规则值不能为空：" + key));
                Assert.isFalse(chars[0] == chars[lastIndex], () -> new ParamsException(key + "参数错误"));
                String column = new String(chars, offset, lastIndex - offset);
                column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column);
                tableUtils.validColumnsName(sqlDefinition, table, column);
                valueCondition.setColumn(column);

                String[] values = text.split(",");

                JsonNode finalValue = value;
                Assert.isFalse(values.length == 2, () -> new ParamsException(key + "与" + finalValue.asText() + "不匹配"));
                String argsKey = table + "_" + column;

                populateSqlDefinition(valueCondition,
                        (char) (62 ^ 60 ^ chars[0]) + (offset == 2 ? "=" : ""),
                        argsKey + "_prefix",
                        values[0],
                        sqlDefinition);


                ValueCondition suffix = ValueCondition.builder()
                        .table(table)
                        .column(valueCondition.getColumn())
                        .combination(valueCondition.getCombination())
                        .conditionBoolean(valueCondition.isConditionBoolean())
                        .build();

                populateSqlDefinition(suffix,
                        chars[lastIndex] + ((length - lastIndex) == 2 ? "=" : ""),
                        argsKey + "_suffix",
                        values[1],
                        sqlDefinition);

                return true;
            }

        } else if ((lastIndex = hasLast(chars)) > 0) {
            operator = chars[lastIndex];
            count = lastIndex;
            value = commonParser.brackets(valueCondition, sqlDefinition.getDataSource(), key, value);
        }
        if (operator == 0) {
            return false;
        }
        String text = value.asText(null);
        Assert.hasNotText(text, () -> new ParamsException("范围规则值不能为空：" + key));
        String column = new String(chars, offset, count);

        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column);

        tableUtils.validColumnsName(sqlDefinition, table, column);
        valueCondition.setColumn(column);
        populateSqlDefinition(valueCondition,
                operator + ((length - count) == 2 ? "=" : ""),
                "range_" + table + "_" + column,
                text,
                sqlDefinition);


        return true;
    }

    private boolean has(char[] chars) {
        return chars[0] == 60 || chars[0] == 62;
    }

    private int hasLast(char[] chars) {
        int index = chars.length - 1;
        if (chars[chars.length - 1] == '=') {
            index = chars.length - 2;
        }
        boolean has = chars[index] == 60 || chars[index] == 62;

        return has ? index : -1;
    }

    private void populateSqlDefinition(ValueCondition valueCondition, String condition, String argName, String argValue, SqlDefinition sqlDefinition) {
        valueCondition.setCondition(condition + ":" + argName);
        valueCondition.setArgName(argName);
        valueCondition.setValue(argValue);
        sqlDefinition.addValueCondition(valueCondition);

    }


    @Override
    public int getOrder() {
        return -300;
    }
}
