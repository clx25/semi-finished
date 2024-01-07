package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.semifinished.annontation.Where;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.Assert;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.bean.TableUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * in查询
 *
 * <pre>
 *     {
 *         "[col]":"v1,v2",
 *         "[col]":["v1","v2"]
 *     }
 * </pre>
 * 以上为查询单个字段的两个写法，都解析为 where col in ('v1','v2')
 *
 * <pre>
 *     {
 *          "[col1,col2]":"(value1,value2),(value3,value4)"
 *          "[col1,col2]":["value1,value2","value3,value4"]
 *     }
 * </pre>
 * 以上为查询多个字段的两种写法，都解析为 where (col1,col2) in ((value1,value2),(value3,value4))
 */

@Slf4j
@Where
@Component
@AllArgsConstructor
public class InParamsParser implements SelectParamsParser {
    private final TableUtils tableUtils;
    private final CommonParser commonParser;

    /**
     * 解析in查询的数据
     *
     * @param value  前端传入的in查询的数据
     * @param length 查询字段数量
     * @return 解析后的数据集合
     */
    private static List<Object> parseValues(JsonNode value, int length) {
        List<Object> values = new ArrayList<>();
        if (value instanceof ArrayNode) {
            List<String> nodes = new ArrayList<>();
            value.elements().forEachRemaining(node -> nodes.add(node.asText()));
            if (length == 1) {
                values.addAll(nodes);
                return values;
            }
            nodes.forEach(node -> {
                String[] multipleValue = node.split(",");
                Assert.isFalse(multipleValue.length == length, () -> new ParamsException("in规则参数数量不匹配：" + nodes));
                values.add(multipleValue);
            });
            return values;
        }
        String text = value.asText();
        if (length == 1) {
            values.addAll(Arrays.asList(text.split(",")));
            return values;
        }

        //分割后根据规则的标准写法进行检测，不处理特殊情况
        String[] inValues = text.split("\\),");

        for (int i = 0; i < inValues.length; i++) {

            String inValue = inValues[i];
            Assert.isFalse(inValue.startsWith("(") && (i != inValues.length - 1 || inValue.endsWith(")")), () -> new ParamsException("参数格式错误：" + text));
            inValue = inValue.substring(1, inValue.length() - (i == inValues.length - 1 ? 1 : 0));

            String[] multipleValue = inValue.split(",");
            Assert.isFalse(multipleValue.length == length, () -> new ParamsException("in规则参数数量不匹配：" + text));
            values.add(multipleValue);
        }

        return values;
    }

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        ValueCondition valueCondition = ParserUtils.columnValue(table, key);
        char[] chars = valueCondition.getColumn().toCharArray();
        int end = chars.length - 1;
        if (chars[0] != '[' || chars[end] != ']') {
            return false;
        }

        value = commonParser.brackets(valueCondition, sqlDefinition.getDataSource(), key, value);

        String column = new String(chars, 1, end - 1);
        String[] inColumns = column.split(",");

        //判断是否多个字段的in查询
        if (inColumns.length > 1) {
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            for (String inColumn : inColumns) {
                inColumn = getColumn(sqlDefinition, table, inColumn.trim());
                joiner.add(table + "." + inColumn);
            }
            column = joiner.toString();
            valueCondition.setTable("");
        } else {
            column = getColumn(sqlDefinition, table, column.trim());
        }

        List<Object> values = parseValues(value, inColumns.length);

        populateValueCondition(sqlDefinition, table, key, valueCondition, values, column);
        return true;
    }

    /**
     * 获取真实字段，检测查询字段
     *
     * @param sqlDefinition SQL定义信息
     * @param table         表名
     * @param column        查询字段
     * @return 监测后的真实字段
     */
    private String getColumn(SqlDefinition sqlDefinition, String table, String column) {
        column = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column);
        tableUtils.validColumnsName(sqlDefinition, table, column);
        return column;
    }


    /**
     * 填充in查询的where查询对象
     *
     * @param sqlDefinition  SQL定义信息
     * @param table          表名
     * @param key            前端传递的key
     * @param valueCondition in查询的where查询对象
     * @param values         查询数据
     * @param column         查询字段
     */
    private void populateValueCondition(SqlDefinition sqlDefinition, String table, String key, ValueCondition valueCondition, List<Object> values, String column) {

        valueCondition.setColumn(column);

        String argName = tableUtils.uniqueAlias("in_" + table + "_");
        boolean conditionBoolean = valueCondition.isConditionBoolean();
        valueCondition.setCondition((conditionBoolean ? "" : "not ") + "in( :" + argName + ")");
        valueCondition.setArgName(argName);
        valueCondition.setValue(values);
        sqlDefinition.addValueCondition(valueCondition);
        if (log.isDebugEnabled()) {
            log.debug("表" + table + "添加查询条件" + key + (conditionBoolean ? "" : "not ") + "in(", argName + ")");
        }
    }


    @Override
    public int getOrder() {
        return -100;
    }
}
