package com.semifinished.api.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.annotation.ApiGroup;
import com.semifinished.api.config.ApiProperties;
import com.semifinished.api.utils.JsonFileUtils;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.utils.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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

    public static final Map<String, String> apiRequestNameGroupMapping = new HashMap<>();

    static {
        //内置的接口name与组名的对应关系
        apiRequestNameGroupMapping.put("SEMI-JSON-API-POST-QUERY", "POSTQ");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-POST", "POST");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-GET", "GET");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-PUT", "PUT");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-POST-BATCH", "POSTB");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-PUT-BATCH", "PUTB");
        apiRequestNameGroupMapping.put("SEMI-JSON-API-DELETE", "DELETE");
        apiRequestNameGroupMapping.put("SEMI_COMMON_MULTI", "MULTI");
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (apiProperties.isCommonApiEnable()) {
            return;
        }
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        addApiRequestNameGroupMapping(handlerMethods.values());

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

        addApi(handlerMethods, apiMap);

        unique(handlerMethods, apiMap);

        semiCache.initHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), apiMap);
    }

    private void addApiRequestNameGroupMapping(Collection<HandlerMethod> handlerMethods) {
        for (HandlerMethod handlerMethod : handlerMethods) {
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(ApiGroup.class)) {
                ApiGroup annotation = method.getAnnotation(ApiGroup.class);
                apiRequestNameGroupMapping.put(method.toString(), annotation.value().toUpperCase());
            }
        }
    }


    /**
     * 解析json文件
     *
     * @param folder json文件目录
     * @param apiMap 解析后的json数据
     */
    public void parseJsonFile(File folder, Map<String, Map<String, ObjectNode>> apiMap) {
        File[] files = folder.listFiles((f, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            return;
        }
        try {
            for (File file : files) {
                if (file.length() == 0) {
                    continue;
                }
                JsonNode json = objectMapper.readTree(file);
                Assert.isFalse(json instanceof ObjectNode, () -> new ConfigException("配置文件%s格式错误", file.getName()));

                json.fields().forEachRemaining(entry -> {
                    JsonNode configs = entry.getValue();
                    String key = entry.getKey().toUpperCase();
                    populateMap(key, (ObjectNode) configs, apiMap, file.getName().replace(".json", ""));
                });

            }
        } catch (IOException e) {
            throw new ConfigException(folder + "json配置文件格式错误");
        }
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
            Assert.isFalse(apiRequestNameGroupMapping.containsValue(groupName), () -> new ConfigException("api配置文件%s：组名%s不存在", fileName, groupName));

            Map<String, ObjectNode> apiConfigs = apiMap.computeIfAbsent(groupName, k -> new HashMap<>());

            String finalPath = path;
            Assert.isTrue(apiConfigs.containsKey(path), () -> new ConfigException("接口%s重复", finalPath));
            JsonNode value = entry.getValue();
            Assert.isFalse(value instanceof ObjectNode, () -> new ConfigException("%s配置错误", value));
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
     * @param handlerMethods api的映射信息
     * @param apiMap         自定义api信息
     */
    private void addApi(Map<RequestMappingInfo, HandlerMethod> handlerMethods, Map<String, Map<String, ObjectNode>> apiMap) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        handlerMethods.forEach((info, handlerMethod) -> {
            Set<String> patternValues = info.getPatternValues();
            String group = getGroupKey(info, handlerMethod);
            if (!apiMap.containsKey(group)) {
                return;
            }
            if (!apiProperties.isCommonApiEnable()) {
                patternValues.clear();
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
     * @param handlerMethods api的映射信息
     */
    private void unique(Map<RequestMappingInfo, HandlerMethod> handlerMethods, Map<String, Map<String, ObjectNode>> apiMap) {
        Map<String, Set<String>> uniqueMap = new HashMap<>();
        handlerMethods.forEach((info, handlerMethod) -> {
            for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                String name = method.name();
                Set<String> remain = uniqueMap.computeIfAbsent(name, k -> new HashSet<>());
                Set<String> patternValues = info.getPatternValues();
                Set<String> uniqueSet = new HashSet<>(remain);

                uniqueSet.retainAll(patternValues);

                Assert.isFalse(uniqueSet.isEmpty(), () -> new ConfigException("接口重复：" + uniqueSet));

                remain.addAll(patternValues);


                String group = getGroupKey(info, handlerMethod);

                if (name.equals(group) || !apiMap.containsKey(group)) {
                    continue;
                }
                apiMap.computeIfAbsent(name, k -> new HashMap<>())
                        .putAll(apiMap.remove(group));
            }
        });

    }


    private String getGroupKey(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        String requestMappingName = requestMappingInfo.getName();
        if (StringUtils.hasText(requestMappingName)) {
            requestMappingName = requestMappingName.toUpperCase();
        }
        String group = apiRequestNameGroupMapping.get(requestMappingName);
        if (!StringUtils.hasText(group)) {
            String name = handlerMethod.getMethod().toString();
            group = apiRequestNameGroupMapping.get(name);
        }
        return group;
    }
}
