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
import lombok.AllArgsConstructor;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Order(0)
@Component
@AllArgsConstructor
public class ApiInit implements ApplicationListener<ContextRefreshedEvent> {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SemiCache semiCache;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationHome applicationHome = new ApplicationHome(getClass());
        File parentFile = applicationHome.getSource().getParentFile();
        File folder = new File(parentFile, configProperties.getApiFolder());

        Map<String, Map<String, ObjectNode>> apiMap = new HashMap<>();
        apiMap.put("GET", new HashMap<>());
        apiMap.put("POST", new HashMap<>());
        apiMap.put("PUT", new HashMap<>());
        apiMap.put("DELETE", new HashMap<>());
        apiMap.put("POSTQ", new HashMap<>());

        try {
            if (folder.exists()) {
                parseJson(folder, apiMap);
            }

            Enumeration<URL> urls = getClass().getClassLoader().getResources("SEMI-API");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                if (resource.exists()) {
                    parseJson(resource.getFile(), apiMap);
                }
            }

        } catch (IOException e) {
            throw new ConfigException("api文件解析错误");
        }

        Set<RequestMappingInfo> requestMappingInfos = requestMappingHandlerMapping.getHandlerMethods().keySet();

        addApi(requestMappingInfos, apiMap);

        unique(requestMappingInfos);

        semiCache.addValue(CoreCacheKey.CUSTOM_API.getKey(), apiMap);
    }

    /**
     * 解析json文件
     *
     * @param folder json文件目录
     * @param apiMap 解析后的json数据
     * @throws IOException 解析错误
     */
    private void parseJson(File folder, Map<String, Map<String, ObjectNode>> apiMap) throws IOException {
        File[] files = folder.listFiles((f, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File jsonFile : files) {
            JsonNode json = objectMapper.readTree(jsonFile);
            Assert.isFalse(json instanceof ObjectNode, () -> new ConfigException(jsonFile.getName() + "api文件格式错误"));
            json.fields().forEachRemaining(entry -> {
                JsonNode configs = entry.getValue();
                Assert.isFalse(configs instanceof ObjectNode, () -> new ConfigException("api文件格式错误"));
                String method = entry.getKey().toUpperCase();

                populateMap(method, (ObjectNode) configs, apiMap);
            });
        }

    }

    /**
     * 转map
     *
     * @param method  请求方式
     * @param configs api配置信息
     * @param apiMap  json数据的填充目标
     */
    private void populateMap(String method, ObjectNode configs, Map<String, Map<String, ObjectNode>> apiMap) {

        configs.fields().forEachRemaining(entry -> {
            String api = entry.getKey();
            if (!api.startsWith("/")) {
                api = "/" + api;
            }
            Assert.isFalse(apiMap.containsKey(method), () -> new ParamsException("请求方式%s配置错误", method));
            Map<String, ObjectNode> apiConfigs = apiMap.get(method);
            String finalApi = api;
            Assert.isTrue(apiConfigs.containsKey(api), () -> new ParamsException("接口%s重复", finalApi));
            JsonNode value = entry.getValue();
            Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("接口%s配置错误", value));
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


            // 关闭通用查询接口
            if (!configProperties.isCommonApiEnable()) {
                patternValues.removeIf("/enhance"::equals);
            }

            if ("SEMI-JSON-API-QUERY".equals(info.getName())) {
                patternValues.addAll(apiMap.get("POSTQ").keySet());
                return;
            }

            if (!"SEMI-JSON-API".equals(info.getName())) {
                return;
            }
            patternValues.removeIf("/enhance"::equals);

            info.getMethodsCondition()
                    .getMethods()
                    .stream()
                    .map(Enum::name)
                    .map(apiMap::get)
                    .filter(Objects::nonNull)
                    .flatMap(paramsMap -> paramsMap.keySet().stream())
                    .forEach(patternValues::add);
        });

        Map<String, ObjectNode> postq = apiMap.remove("POSTQ");
        if (postq != null) {
            apiMap.get("POST").putAll(postq);
        }


    }

    /**
     * api唯一校验
     *
     * @param requestMappingInfos api的映射信息
     */
    private void unique(Set<RequestMappingInfo> requestMappingInfos) {
        Map<String, Set<String>> uniqueMap = new HashMap<>();

        for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
            for (RequestMethod method : requestMappingInfo.getMethodsCondition().getMethods()) {
                Set<String> remain = uniqueMap.computeIfAbsent(method.name(), k -> new HashSet<>());
                Set<String> patternValues = requestMappingInfo.getPatternValues();
                Set<String> uniqueSet = new HashSet<>(remain);

                uniqueSet.retainAll(patternValues);

                Assert.isFalse(uniqueSet.isEmpty(), () -> new ParamsException("接口重复：" + uniqueSet));

                remain.addAll(patternValues);
            }
        }
    }
}
