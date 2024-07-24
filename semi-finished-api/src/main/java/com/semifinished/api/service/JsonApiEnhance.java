package com.semifinished.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.config.ApiProperties;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Component
@AllArgsConstructor
@Order(Integer.MIN_VALUE + 500)
public class JsonApiEnhance implements AfterQueryEnhance, AfterUpdateEnhance {

    private final ApiProperties apiProperties;
    private final SemiCache semiCache;

    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        ObjectNode mergeParams = mergeParams(params, false);
        params.removeAll();
        params.setAll(mergeParams);
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
        if (apiProperties.isCommonApiEnable()) {
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



