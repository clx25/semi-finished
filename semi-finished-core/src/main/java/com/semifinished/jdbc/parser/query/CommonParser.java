package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.annontation.Where;
import com.semifinished.config.ConfigProperties;
import com.semifinished.config.DataSourceConfig;
import com.semifinished.config.DataSourceProperties;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.util.Assert;
import com.semifinished.util.MapUtils;
import com.semifinished.util.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommonParser {
    private final ConfigProperties configProperties;
    private final DataSourceProperties dataSourceProperties;
    @Resource
    private KeyValueParamsParserExecutor keyValueParamsParserExecutor;

    @Where
    private List<KeyValueParamsParser> paramsParsers;

    /**
     * 获取实际的值
     *
     * @param mapping 映射关系
     * @param key     key
     * @return 实际的值
     */
    private static String actual(Map<String, String> mapping, String key) {
        if (MapUtils.isEmpty(mapping)) {
            return key;
        }

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (key.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        Assert.isFalse(mapping.get(key) == null, () -> new ParamsException("参数" + key + "错误"));

        return key;
    }

    /**
     * 解析括号规则
     * <pre>
     *     {
     *         "col1":{
     *             "bracketsKey":"value1",
     *             "col2":"value2"
     *         },
     *         "!col3":"value3"
     *     }
     * </pre>
     * 以上解析为 (col1=value2 and col2=value2) or col3=value3
     *
     * @param valueCondition where条件实体类
     * @param key            请求规则key，如上方的col1
     * @param value          规则value,如上方{ "bracketsKey":"value1",  "col2":"value2"}
     * @return 根据bracketsKey获取的值
     */
    public JsonNode brackets(ValueCondition valueCondition, String key, JsonNode value) {
        if (value instanceof ObjectNode) {
            ObjectNode params = (ObjectNode) value;
            SqlDefinition sqlDefinition = new SqlDefinition(valueCondition.getTable(), params);

            value = params.remove(configProperties.getBracketsKey());
            Assert.isTrue(value == null, () -> new ParamsException(key + "参数错误"));
            keyValueParamsParserExecutor.parser(params, sqlDefinition, paramsParsers);

            valueCondition.addBracketsAll(sqlDefinition.getValueCondition());
        }


        return value;
    }

    /**
     * 获取实际表名
     *
     * @param tableKey 表名key
     * @return 实际表名
     */
    public String getActualTable(String dataSource, String tableKey) {
        DataSourceConfig.Mapping mapping = mapping(dataSource);
        return mapping.isEnable() ? actual(mapping.getTable(), tableKey) : tableKey;
    }

    /**
     * 通过配置的字段名映射获取实际字段名
     * 通过匹配value返回key，如果没在value中找到，但是在key中找到了，会抛出异常
     *
     * @param dataSource 数据源
     * @param table      表名
     * @param column     字段名
     * @return 实际字段名
     */
    public String getActualColumn(String dataSource, String table, String column) {
        DataSourceConfig.Mapping mapping = mapping(dataSource);
        if (!mapping.isEnable()) {
            return column;
        }

        Map<String, Map<String, String>> columnMapping = mapping.getColumn();
        if (MapUtils.isEmpty(columnMapping)) {
            return column;
        }

        return actual(columnMapping.get(table), column);
    }

    /**
     * 通过配置的字段名映射获取实际的名称作为别名
     *
     * @param dataSource 数据源
     * @param table      表名
     * @param column     字段名
     * @return 实际字段名
     */
    public String getActualAlias(String dataSource, String table, String column) {
        DataSourceConfig.Mapping mapping = mapping(dataSource);
        if (!mapping.isEnable()) {
            return column;
        }

        Map<String, Map<String, String>> columnMapping = mapping.getColumn();
        if (MapUtils.isEmpty(columnMapping)) {
            return column;
        }

        Map<String, String> columns = columnMapping.get(table);
        if (MapUtils.isEmpty(columns)) {
            return column;
        }
        String actual = columns.get(column);

        return ParamsUtils.hasText(actual, column);
    }


    public DataSourceConfig.Mapping mapping(String dataSource) {
        return dataSourceProperties.getDataSource().get(dataSource).getMapping();
    }
}
