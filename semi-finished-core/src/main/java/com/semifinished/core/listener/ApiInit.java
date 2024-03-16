package com.semifinished.core.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.JsonFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Order(0)
@Component
@RequiredArgsConstructor
public class ApiInit implements ApplicationListener<ContextRefreshedEvent> {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SemiCache semiCache;

    private final Map<String, String> jsonConfig = new HashMap<>();

    {
        jsonConfig.put("SEMI-JSON-API-POSTQ", "POSTQ");
        jsonConfig.put("SEMI-JSON-API-POST", "POST");
        jsonConfig.put("SEMI-JSON-API-GET", "GET");
        jsonConfig.put("SEMI-JSON-API-PUT", "PUT");
        jsonConfig.put("SEMI-JSON-API-POSTB", "POSTB");
        jsonConfig.put("SEMI-JSON-API-PUTB", "PUTB");
    }

    public Map<String, String> getJsonConfig() {
        return jsonConfig;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Map<String, ObjectNode>> apiMap = new HashMap<>();


        File folder = JsonFileUtils.jarFile(configProperties);
        if (folder.exists()) {
            parseJson(folder, apiMap);
        }

        for (File file : JsonFileUtils.classPatchFiles(configProperties)) {
            if (file.exists()) {
                parseJson(file, apiMap);
            }
        }

        Set<RequestMappingInfo> requestMappingInfos = requestMappingHandlerMapping.getHandlerMethods().keySet();

        addApi(requestMappingInfos, apiMap);

        unique(requestMappingInfos, apiMap);

        semiCache.addValue(CoreCacheKey.CUSTOM_API.getKey(), apiMap);
    }

    /**
     * 解析json文件
     *
     * @param folder json文件目录
     * @param apiMap 解析后的json数据
     */
    private void parseJson(File folder, Map<String, Map<String, ObjectNode>> apiMap) {

        for (ObjectNode json : JsonFileUtils.parseJsonFile(folder, objectMapper)) {
            json.fields().forEachRemaining(entry -> {
                JsonNode configs = entry.getValue();
                Assert.isFalse(configs instanceof ObjectNode, () -> new ConfigException("api文件格式错误"));
                String key = entry.getKey().toUpperCase();

                populateMap(key, (ObjectNode) configs, apiMap);
            });
        }


    }

    /**
     * 转map
     *
     * @param groupName 组名
     * @param configs   api配置信息
     * @param apiMap    json数据的填充目标
     */
    private void populateMap(String groupName, ObjectNode configs, Map<String, Map<String, ObjectNode>> apiMap) {

        configs.fields().forEachRemaining(entry -> {
            String api = entry.getKey();
            if (!api.startsWith("/")) {
                api = "/" + api;
            }
            Assert.isFalse(jsonConfig.containsValue(groupName), () -> new ConfigException("未配置该组名%s对应的方法", groupName));

            Map<String, ObjectNode> apiConfigs = apiMap.computeIfAbsent(groupName, k -> new HashMap<>());

            String finalApi = api;
            Assert.isTrue(apiConfigs.containsKey(api), () -> new ParamsException("%s重复", finalApi));
            JsonNode value = entry.getValue();
            Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("%s配置错误", value));
            apiConfigs.put(api, (ObjectNode) value);
        });

    }


    /**
     * 添加api映射到spring的配置
     *
     * @param requestMappingInfos api的映射信息
     * @param apiMap              自定义api信息
     */
    private void addApi(Set<RequestMappingInfo> requestMappingInfos, Map<String, Map<String, ObjectNode>> apiMap) {

        requestMappingInfos.forEach(info -> {
            Set<String> patternValues = info.getPatternValues();

            String requestMappingName = info.getName();
            String group = jsonConfig.get(requestMappingName);
            if (!StringUtils.hasText(group)) {
                return;
            }
            if (!configProperties.isCommonApiEnable()) {
                patternValues.clear();
            }
            if (!apiMap.containsKey(group)) {
                return;
            }
            patternValues.addAll(apiMap.getOrDefault(group, new HashMap<>()).keySet());

        });


    }

    /**
     * api唯一校验
     *
     * @param requestMappingInfos api的映射信息
     */
    private void unique(Set<RequestMappingInfo> requestMappingInfos, Map<String, Map<String, ObjectNode>> apiMap) {
        Map<String, Set<String>> uniqueMap = new HashMap<>();

        for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
            for (RequestMethod method : requestMappingInfo.getMethodsCondition().getMethods()) {
                String name = method.name();
                Set<String> remain = uniqueMap.computeIfAbsent(name, k -> new HashSet<>());
                Set<String> patternValues = requestMappingInfo.getPatternValues();
                Set<String> uniqueSet = new HashSet<>(remain);

                uniqueSet.retainAll(patternValues);

                Assert.isFalse(uniqueSet.isEmpty(), () -> new ParamsException("接口重复：" + uniqueSet));

                remain.addAll(patternValues);


                String group = jsonConfig.get(requestMappingInfo.getName());

                if (name.equals(group)||!apiMap.containsKey(group)) {
                    continue;
                }
                apiMap.computeIfAbsent(name, k -> new HashMap<>())
                        .putAll(apiMap.remove(group));

            }
        }
    }
}
