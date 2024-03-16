package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.config.DataSourceConfig;
import com.semifinished.core.config.DataSourceProperties;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class CommonParser {
    private final ConfigProperties configProperties;
    private final DataSourceProperties dataSourceProperties;
    @Lazy
    @Resource
    private List<ParamsParser> paramsParsers;


    /**
     * 获取实际的值
     *
     * @param mapping 映射关系
     * @param key     key
     * @return 实际的值
     */
    private static String actual(Map<String, String> mapping, String key) {
        if (CollectionUtils.isEmpty(mapping)) {
            return key;
        }

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (key.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        Assert.isFalse(mapping.get(key) == null, () -> new ParamsException("参数错误：" + key));

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
     * @param dataSource     数据源
     * @param key            请求规则key，如上方的col1
     * @param value          规则value,如上方{ "bracketsKey":"value1",  "col2":"value2"}
     * @return 根据bracketsKey获取的值
     */
    public JsonNode brackets(ValueCondition valueCondition, String dataSource, String key, JsonNode value) {
        if (!(value instanceof ObjectNode)) {
            return value;
        }
        ObjectNode params = (ObjectNode) value;
        SqlDefinition sqlDefinition = new SqlDefinition(valueCondition.getTable(), params);
        sqlDefinition.setDataSource(dataSource);
        sqlDefinition.setStatus(ParserStatus.BRACKET.getStatus());
        value = params.remove(configProperties.getBracketsKey());
        Assert.isTrue(value == null, () -> new ParamsException(key + "参数错误"));
        if (params.isEmpty()) {
            return value;
        }

        paramsParsers.forEach(parser -> parser.parse(params, sqlDefinition));

        valueCondition.addBracketsAll(sqlDefinition.getValueCondition());


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
        return mapping != null && mapping.isEnable() ? actual(mapping.getTable(), tableKey) : tableKey;
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

        return actual(getColumnMapping(dataSource, table), column);
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
        Map<String, String> columnMapping = getColumnMapping(dataSource, table);
        String actual = columnMapping.get(column);

        return ParamsUtils.hasText(actual, column);
    }

    /**
     * 获取字段映射
     *
     * @param dataSource 数据源名称
     * @param table      表名
     * @return 字段映射
     */
    public Map<String, String> getColumnMapping(String dataSource, String table) {
        DataSourceConfig.Mapping mapping = mapping(dataSource);
        if (mapping == null || !mapping.isEnable()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> columnMapping = mapping.getColumn();
        if (CollectionUtils.isEmpty(columnMapping)) {
            return Collections.emptyMap();
        }

        Map<String, String> columns = columnMapping.get(table);
        if (CollectionUtils.isEmpty(columns)) {
            return Collections.emptyMap();
        }
        return columns;
    }


    /**
     * 获取映射数据
     *
     * @param dataSource 数据源
     * @return 映射数据
     */
    public DataSourceConfig.Mapping mapping(String dataSource) {
        if (!StringUtils.hasText(dataSource)) {
            dataSource = configProperties.getDataSource();
        }
        return dataSourceProperties.getDataSource().get(dataSource).getMapping();
    }
}
