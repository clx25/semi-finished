package com.semifinished.api.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.config.ApiProperties;
import com.semifinished.api.utils.JsonFileUtils;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Order(0)
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonApiInit implements ApplicationListener<ContextRefreshedEvent> {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ApiProperties apiProperties;
    private final ObjectMapper objectMapper;
    private final SemiCache semiCache;

    private final Map<String, String> jsonConfig = new HashMap<>();

    {
        //内置的接口name与组名的对应关系
        jsonConfig.put("SEMI-JSON-API-POST-QUERY", "POSTQ");
        jsonConfig.put("SEMI-JSON-API-POST", "POST");
        jsonConfig.put("SEMI-JSON-API-GET", "GET");
        jsonConfig.put("SEMI-JSON-API-PUT", "PUT");
        jsonConfig.put("SEMI-JSON-API-POST-BATCH", "POSTB");
        jsonConfig.put("SEMI-JSON-API-PUT-BATCH", "PUTB");
    }

    public Map<String, String> getJsonConfig() {
        return jsonConfig;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (apiProperties.isCommonApiEnable()) {
            return;
        }
        Map<String, Map<String, ObjectNode>> apiMap = new HashMap<>();

        File folder = JsonFileUtils.jarFile(apiProperties);
        if (folder.exists()) {
            parseJsonFile(folder, apiMap);
        }

        //获取类路径api json文件夹路径
        for (File file : JsonFileUtils.classPathFiles(apiProperties)) {
            if (file.exists()) {
                parseJsonFile(file, apiMap);
            }
        }

        Set<RequestMappingInfo> requestMappingInfos = requestMappingHandlerMapping.getHandlerMethods().keySet();

        addApi(requestMappingInfos, apiMap);

        unique(requestMappingInfos, apiMap);

        semiCache.initHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), apiMap);
    }


    /**
     * 解析json文件
     *
     * @param folder json文件目录
     * @param apiMap 解析后的json数据
     */
    public List<ObjectNode> parseJsonFile(File folder, Map<String, Map<String, ObjectNode>> apiMap) {
        File[] files = folder.listFiles((f, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        List<ObjectNode> nodes = new ArrayList<>();
        try {
            for (File file : files) {
                if (file.length() == 0) {
                    continue;
                }
                JsonNode json = objectMapper.readTree(file);
                Assert.isFalse(json instanceof ObjectNode, () -> new ConfigException("配置文件%s格式错误", file.getName()));
                ObjectNode node = (ObjectNode) json;
                json.fields().forEachRemaining(entry -> {
                    JsonNode configs = entry.getValue();
                    String key = entry.getKey().toUpperCase();
                    populateMap(key, (ObjectNode) configs, apiMap, file.getName().replace(".json", ""));
                });

                nodes.add(node);
            }
        } catch (IOException e) {
            throw new ConfigException(folder + "json配置文件格式错误");
        }
        return nodes;
    }

    /**
     * 转map
     * group->(path->config)
     *
     * @param groupName 组名
     * @param configs   api配置信息
     * @param apiMap    json数据的填充目标
     * @param fileName  文件名
     */
    private void populateMap(String groupName, ObjectNode configs, Map<String, Map<String, ObjectNode>> apiMap, String fileName) {

        configs.fields().forEachRemaining(entry -> {
            String path = entry.getKey();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            Map<String, ObjectNode> apiConfigs = apiMap.computeIfAbsent(groupName, k -> new HashMap<>());

            String finalPath = path;
            Assert.isTrue(apiConfigs.containsKey(path), () -> new ParamsException("%s重复", finalPath));
            JsonNode value = entry.getValue();
            Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("%s配置错误", value));
            ObjectNode pathConfig = (ObjectNode) value;
            if (!pathConfig.has("tag")) {
                pathConfig.put("tag", fileName);
            }
            apiConfigs.put(path, pathConfig);
        });

    }


    /**
     * 添加api映射到spring的配置
     *
     * @param requestMappingInfos api的映射信息
     * @param apiMap              自定义api信息
     */
    private void addApi(Set<RequestMappingInfo> requestMappingInfos, Map<String, Map<String, ObjectNode>> apiMap) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        requestMappingInfos.forEach(info -> {
            Set<String> patternValues = info.getPatternValues();
            String requestMappingName = info.getName();
            if (StringUtils.hasText(requestMappingName)) {
                requestMappingName=requestMappingName.toUpperCase();
            }
            String group = jsonConfig.get(requestMappingName);
            if (!StringUtils.hasText(group)) {
                return;
            }
            if (!apiProperties.isCommonApiEnable()) {
                patternValues.clear();
            }
            if (!apiMap.containsKey(group)) {
                return;
            }

            Map<String, ObjectNode> apiConfig = apiMap.getOrDefault(group, new HashMap<>());


            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            apiConfig.forEach((path, config) -> {
                //添加path到当前controller方法，此时就可以通过path请求，spring会调用当前controller方法进行处理
                patternValues.add(path);

                //把api对应的实际请求方式保存到配置中，提供给api文档展示
                ArrayNode methodNode = config.withArray("method");
                List<String> methodlist = methods.stream()
                        .map(Enum::name)
                        .peek(methodNode::add)
                        .collect(Collectors.toList());

                //全局crossOrigin配置为false时才启用接口级的crossOrigin配置
                if (!apiProperties.isCrossOrigin()) {
                    populateCorsConfig(source, path, config, methodlist);
                }

            });

        });
        //全局crossOrigin配置
        if (apiProperties.isCrossOrigin()) {
            populateCorsConfig(source, "/**", Collections.singletonList("*"),
                    Arrays.asList("GET", "POST", "PUT", "DELETE"));
        }

        //注册自定义crossOrigin配置替换默认配置
        requestMappingHandlerMapping.setCorsConfigurationSource(source);
    }

    private void populateCorsConfig(UrlBasedCorsConfigurationSource source, String path, ObjectNode config, List<String> methods) {
        if (!config.has("crossOrigin")) {
            return;
        }

        String crossOrigin = config.path("crossOrigin").asText();

        populateCorsConfig(source, path, Arrays.asList(crossOrigin.split(",")), methods);
    }

    private void populateCorsConfig(UrlBasedCorsConfigurationSource source, String pattern, List<String> origins, List<String> methods) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(methods);
        configuration.addAllowedHeader("*");
        source.registerCorsConfiguration(pattern, configuration);
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

                if (name.equals(group) || !apiMap.containsKey(group)) {
                    continue;
                }
                apiMap.computeIfAbsent(name, k -> new HashMap<>())
                        .putAll(apiMap.remove(group));

            }
        }
    }

}
