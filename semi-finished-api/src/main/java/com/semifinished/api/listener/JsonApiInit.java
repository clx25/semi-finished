package com.semifinished.api.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.annotation.ApiGroup;
import com.semifinished.api.config.ApiConfigurer;
import com.semifinished.api.config.ApiProperties;
import com.semifinished.api.utils.JsonFileUtils;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.utils.Assert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
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
    private final List<ApiConfigurer> apiConfigurers;
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
        //如果开启通用查询规则，则直接返回，不解析json文件
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

        for (ApiConfigurer apiConfigurer : apiConfigurers) {
            apiConfigurer.addApiMap(apiMap);
        }

        Map<String, Map<String, ObjectNode>> allPath = uniqueVersion(handlerMethods, apiMap);

        addApi(handlerMethods, apiMap);

        unique(handlerMethods);

        semiCache.initHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), allPath);
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
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.length() == 0) {
                continue;
            }
            JsonNode json;
            try {
                json = objectMapper.readTree(file);
            } catch (IOException e) {
                throw new ConfigException(file + "配置文件格式错误", e);
            }
            Assert.isTrue(json instanceof ObjectNode, () -> new ConfigException("配置文件%s格式错误", file.getName()));

            json.fields().forEachRemaining(entry -> {
                JsonNode configs = entry.getValue();
                String key = entry.getKey().toUpperCase();
                populateMap(key, (ObjectNode) configs, apiMap, file.getName().replace(".json", ""));
            });

        }
    }

    /**
     * 转map
     * group->(path->config)
     *
     * @param groupName 组名
     * @param configs   json文件解析的api配置信息
     * @param apiMap    json数据的填充目标
     * @param fileName  文件名
     */
    private void populateMap(String groupName, ObjectNode configs, Map<String, Map<String, ObjectNode>> apiMap, String fileName) {
        configs.fields().forEachRemaining(entry -> {
            String path = entry.getKey();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            Assert.isTrue(apiRequestNameGroupMapping.containsValue(groupName), () -> new ConfigException("api配置文件%s：组名%s不存在", fileName, groupName));

            Map<String, ObjectNode> apiConfigs = apiMap.computeIfAbsent(groupName, k -> new HashMap<>());

            //已存在的配置
            ObjectNode oldConfig = apiConfigs.get(path);

            //新的配置
            JsonNode value = entry.getValue();
            Assert.isTrue(value instanceof ObjectNode, () -> new ConfigException("%s配置错误", value));
            ObjectNode newConfig = (ObjectNode) value;

            //如果旧的配置不为空，表示有重复，则比较版本
            if (oldConfig != null) {
                double oldVersion = oldConfig.path("version").asDouble(0);
                double newVersion = newConfig.path("version").asDouble(0);
                String finalPath = path;
                Assert.isFalse(oldVersion == newVersion, () -> new ConfigException("接口%s重复", finalPath));

                //如果已存在的版本高，则直接舍弃新的版本
                if (oldVersion > newVersion) {
                    return;
                }
                //如果新的版本高，则删除旧版
                apiConfigs.remove(path);
            }


            if (!newConfig.has("tag")) {
                newConfig.put("tag", fileName);
            }
            apiConfigs.put(path, newConfig);
        });

    }

    private Map<String, Map<String, ObjectNode>> uniqueVersion(Map<RequestMappingInfo, HandlerMethod> handlerMethods, Map<String, Map<String, ObjectNode>> apiMap) {
        Map<String, Map<String, ObjectNode>> allPath = new HashMap<>();
        Map<Pair<String, String>, String> groupPathMap = new HashMap<>();
        List<Pair<String, String>> removePaths = new ArrayList<>();
        handlerMethods.forEach((info, handlerMethod) -> {
            for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                String requestMethodName = method.name();
                String group = getGroupKey(info, handlerMethod);
                Map<String, ObjectNode> config = apiMap.get(group);
                if (config == null) {
                    return;
                }
                Map<String, ObjectNode> methodConfigs = allPath.computeIfAbsent(requestMethodName, k -> new HashMap<>());
                //如果存在相同请求方式，相同path的请求，则判断版本
                Iterator<Map.Entry<String, ObjectNode>> iterator = config.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ObjectNode> entry = iterator.next();

                    String path = entry.getKey();
                    ObjectNode oldConfig = methodConfigs.get(path);
                    ObjectNode newConfig = entry.getValue();
                    if (oldConfig == null) {
                        methodConfigs.put(path, newConfig);
                        groupPathMap.put(new Pair<>(requestMethodName, path), group);
                        continue;
                    }

                    //旧
                    String version = oldConfig.path("version").asText("");
                    //新
                    String newVersion = newConfig.path("version").asText("");

                    Assert.isFalse(version.equals(newVersion), () -> new ConfigException("接口重复：" + path));

                    int compare = version.compareTo(newVersion);

                    if (compare > 0) {
                        iterator.remove();
                    } else {
                        String oldGroup = groupPathMap.get(new Pair<>(requestMethodName, path));
                        removePaths.add(new Pair<>(oldGroup, path));
                        methodConfigs.remove(path);
                        methodConfigs.put(path, newConfig);
                    }

                }
            }
        });
        for (Pair<String, String> removePath : removePaths) {
            apiMap.get(removePath.getKey()).remove(removePath.getValue());
        }
        return allPath;
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

            //获取该请求的组名
            String group = getGroupKey(info, handlerMethod);

            //如果不是配置中的组名，则表示不是系统增强的接口，直接返回
            if (!apiMap.containsKey(group)) {
                return;
            }

            //清除通用接口path
            patternValues.clear();

            //获取该组名对应的请求配置
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
    private void unique(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        Map<String, Set<String>> uniqueMap = new HashMap<>();
        handlerMethods.forEach((info, handlerMethod) -> {
            for (RequestMethod method : info.getMethodsCondition().getMethods()) {
                String name = method.name();
                Set<String> remain = uniqueMap.computeIfAbsent(name, k -> new HashSet<>());
                Set<String> patternValues = info.getPatternValues();
                Set<String> uniqueSet = new HashSet<>(remain);

                uniqueSet.retainAll(patternValues);

                Assert.isTrue(uniqueSet.isEmpty(), () -> new ConfigException("接口重复：" + uniqueSet));

                remain.addAll(patternValues);
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
