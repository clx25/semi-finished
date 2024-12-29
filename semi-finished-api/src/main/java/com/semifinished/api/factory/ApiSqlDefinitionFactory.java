package com.semifinished.api.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.excetion.ApiException;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.facotry.SqlDefinitionFactory;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

@AllArgsConstructor
public class ApiSqlDefinitionFactory implements SqlDefinitionFactory {
    private final SemiCache semiCache;

    /**
     * 在进入解析流程前处理请求参数的替换规则，而不是放到解析中进行，因为解析器前的增强器可能会使用到替换后的数据
     *
     * @param params 请求参数
     * @return SQL定义信息
     */
    @Override
    public SqlDefinition getSqlDefinition(JsonNode params) {
        return getSqlDefinition(getTemplate(), params);
    }

    public SqlDefinition getSqlDefinition(JsonNode template, JsonNode params) {
        SqlDefinition definition = new SqlDefinition();
        definition.setRawParams(params.deepCopy());
        ObjectNode objectNode = (ObjectNode) deepMerge(template, params);
        Assert.isTrue(objectNode.isEmpty(), () -> new ParamsException("参数不能为空"));
        definition.setParams(objectNode);
        return definition;
    }


    public ObjectNode getTemplate() {
        JsonNode apiInfos = getApiInfos();
        if (apiInfos.isMissingNode()) {
            return null;
        }
        return apiInfos.with("params");
    }

    public JsonNode getApiInfos() {

        HttpServletRequest request = RequestUtils.getRequest();
        String servletPath = request.getServletPath();
        String method = request.getMethod();

        Map<String, JsonNode> apiMaps = semiCache.getHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), method);

        if (apiMaps == null) {
            return MissingNode.getInstance();
        }
        return apiMaps.getOrDefault(servletPath, MissingNode.getInstance());
    }


    /**
     * 递归深度合并参数
     *
     * @param template 请求对应的参数模板
     * @param params   请求参数
     * @return 合并后的参数
     */
    public JsonNode deepMerge(JsonNode template, JsonNode params) {

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
    private void populate(JsonNode params, ObjectNode jsonNode, String rawKey, JsonNode value) {
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
            value = parseBatch(value, params);
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
    private JsonNode parseBatch(JsonNode template, JsonNode params) {
        if (params instanceof ArrayNode) {
            ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();
            ArrayNode arrayNode = (ArrayNode) params;
            for (JsonNode jsonNode : arrayNode) {
                JsonNode mergeItem = deepMerge(template, jsonNode.deepCopy());
                jsonNodes.add(mergeItem);
            }
            return jsonNodes;
        }

        if (template instanceof ObjectNode) {
            template = deepMerge(template, params);
            return parse((ObjectNode) template);
        }

        ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();

        for (JsonNode node : template) {
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
                objectNode.set(key, params.get(name));
            });
            jsonNodes.add(objectNode);
        }


        return jsonNodes;
    }

    private ArrayNode parse(ObjectNode params) {
        String arrayName = null;
        Iterator<String> iterator = params.fieldNames();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.startsWith("[") && name.endsWith("]")) {
                arrayName = name;
            }
        }
        Assert.hasNotText(arrayName, () -> new ApiException("api配置错误"));


        ArrayNode arrayNodes = JsonNodeFactory.instance.arrayNode();

        JsonNode jsonNode = params.get(arrayName);
        params = params.without(arrayName);
        String msg = "参数" + arrayName + "数据类型错误";
        arrayName = arrayName.substring(1, arrayName.length() - 1);
        Assert.isTrue(jsonNode instanceof ObjectNode, () -> new ParamsException(msg));
        if (jsonNode instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (JsonNode node : arrayNode) {
                params = params.deepCopy();
                params.set(arrayName, node);
                arrayNodes.add(params);
            }
        }
        return arrayNodes;
    }
}
