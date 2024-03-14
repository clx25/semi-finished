package com.semifinished.core.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.CodeException;
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
    private final SemiCache semiCache;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        HttpServletRequest request = RequestUtils.getRequest();
        ObjectNode params = sqlDefinition.getParams();
        String method = request.getMethod();
        Map<String, ObjectNode> apiMaps = semiCache.getValue(CoreCacheKey.CUSTOM_API.getKey(), method);
        String servletPath = request.getServletPath();
        if (apiMaps == null) {
            return false;
        }
        ObjectNode apiInfos = apiMaps.get(servletPath);

        if (apiInfos == null || apiInfos.isEmpty()) {
            return false;
        }


        ObjectNode mergeParams = mergeParams(apiInfos, params);

        params.removeAll();
        params.setAll(mergeParams);
        return false;
    }


    /**
     * 合并参数
     *
     * @param apiConfigs 配置信息
     * @param params     请求参数
     * @return 合并后的参数
     */
    private ObjectNode mergeParams(ObjectNode apiConfigs, ObjectNode params) {
        ObjectNode template = (ObjectNode) apiConfigs.get("params");
        if (template.isEmpty()) {
            return template;
        }

        return (ObjectNode) deepMerge(template, params);
    }

    /**
     * 递归深度合并参数
     *
     * @param template 请求对应的参数模板
     * @param params   请求参数
     * @return 合并后的参数
     */
    private JsonNode deepMerge(JsonNode template, ObjectNode params) {

        if (!(template instanceof ObjectNode)) {
            return template;
        }
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        template.fields().forEachRemaining(entry -> {
            //包含$$的参数键
            String key = entry.getKey();
            //参数值或者$$规则的替换关联字段
            JsonNode value = entry.getValue();
            if (value instanceof ArrayNode) {
                ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();
                for (JsonNode node : value) {
                    jsonNodes.add(deepMerge(node, params));
                }
                value = jsonNodes;

            } else if (value instanceof ObjectNode) {
                value = deepMerge(value, params);
            } else {

                if (!key.endsWith("$$")) {
                    jsonNode.set(key, value);
                    return;
                }
                //替换数据的key
                String name = value.asText("");
                Assert.hasNotText(name, () -> new CodeException("$$规则关联字段不能为空：" + entry.getKey()));
                key = key.substring(0, key.length() - 2);
                if (!params.has(name)) {
                    return;
                }
                value = params.get(name);
            }

            jsonNode.set(key, value);
        });
        return jsonNode;
    }
}



