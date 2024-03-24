package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.config.DataSourceConfig;
import com.semifinished.core.config.DataSourceProperties;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class CommonParser {
    private final ConfigProperties configProperties;
    private final DataSourceProperties dataSourceProperties;
    private final SemiCache semiCache;
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


    /**
     * 合并参数
     *
     * @param params    请求参数
     * @param returnRaw 当没有合适的合并参数时，是返回原始数据还是抛出异常，true返回原始数据，false抛出异常
     * @return 合并后的参数
     */
    public ObjectNode mergeParams(ObjectNode params, boolean returnRaw) {
        ObjectNode deepParams = params.deepCopy();
        if (configProperties.isCommonApiEnable()) {
            return deepParams;
        }
        HttpServletRequest request = RequestUtils.getRequest();

        String method = request.getMethod();
        Map<String, JsonNode> apiMaps = semiCache.getHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), method);
        String servletPath = request.getServletPath();
        if (returnRaw && apiMaps == null) {
            return deepParams;
        }
        Assert.isTrue(!returnRaw && apiMaps == null, () -> new ParamsException("请求没有对应模板"));

        JsonNode apiInfos = apiMaps.getOrDefault(servletPath, MissingNode.getInstance());

        JsonNode template = apiInfos.get("params");
        if (returnRaw && !(template instanceof ObjectNode)) {
            return deepParams;
        }
        Assert.isTrue(!returnRaw && !(template instanceof ObjectNode), () -> new ParamsException("请求没有对应模板"));

        return (ObjectNode) deepMerge(template, deepParams);
    }

    /**
     * 递归深度合并参数
     *
     * @param template 请求对应的参数模板
     * @param params   请求参数
     * @return 合并后的参数
     */
    private JsonNode deepMerge(JsonNode template, ObjectNode params) {

        if (template instanceof ArrayNode) {
            ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();
            template.forEach(node -> jsonNodes.add(deepMerge(node, params)));
            return jsonNodes;
        }


        if (!(template instanceof ObjectNode)) {
            return template;
        }
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        template.fields().forEachRemaining(entry -> populate(params, jsonNode, entry.getKey(), entry.getValue()));
        return jsonNode;
    }

    /**
     * 替换并填充数据
     *
     * @param params   请求参数
     * @param jsonNode 需要填充的数据
     * @param rawKey   json配置参数的key
     * @param value    json配置参数的value
     */
    private void populate(ObjectNode params, ObjectNode jsonNode, String rawKey, JsonNode value) {
        String key = rawKey;
        if (key.endsWith("$$")) {
            key = key.substring(0, key.length() - 2);
            //替换数据的key
            String name = value.asText("");

            Assert.hasNotText(name, () -> new CodeException("$$规则关联字段不能为空：" + rawKey));
            if (!params.has(name)) {
                return;
            }
            value = params.get(name);
            jsonNode.set(key, value);
            return;
        }

        if ("@batch".equals(key)) {
            value = parseBatch((ObjectNode) value, params);
        } else if (value instanceof ArrayNode || value instanceof ObjectNode) {
            value = deepMerge(value, params);
        }

        jsonNode.set(key, value);
    }


    /**
     * 解析批量请求参数
     *
     * @param template 参数模板
     * @param params   请求参数
     * @return 合并后的参数
     */
    private JsonNode parseBatch(ObjectNode template, ObjectNode params) {
        JsonNode batchNodes = params.get("@batch");

        ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();

        for (JsonNode node : batchNodes) {
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            template.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (!key.endsWith("$$")) {
                    objectNode.set(key, value);
                    return;
                }
                key = key.substring(0, key.length() - 2);
                String name = value.asText();
                objectNode.set(key, node.get(name));
            });
            jsonNodes.add(objectNode);
        }


        return jsonNodes;
    }
}
