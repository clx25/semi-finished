package com.semifinished.web.config;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.config.ApiConfigurer;
import com.semifinished.auth.config.AuthConfigurer;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.web.pojo.Api;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 将数据库的api配置添加到系统
 */
@Component
@AllArgsConstructor
public class ApiMapConfigurer implements ApiConfigurer, AuthConfigurer {

    private final SqlExecutorHolder sqlExecutorHolder;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void addApiMap(Map<String, Map<String, ObjectNode>> apiMap) {

        List<Api> semiApis = sqlExecutorHolder.dataSource().list("select * from semi_api", Api.class);
        semiApis.stream().filter(api -> {
            Map<String, ObjectNode> map = apiMap.get(api.getGroupName());
            if (map == null) {
                return true;
            }
            for (String pattern : map.keySet()) {
                if (pathMatcher.match(pattern, api.getPattern())) {
                    return false;
                }
            }
            return true;
        }).forEach(api -> {
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            objectNode.put("summary", api.getSummary());
            objectNode.put("version", api.getVersion());
            objectNode.put("params", api.getParams());
            objectNode.put("config", api.getConfig());
            Map<String, ObjectNode> map = apiMap.computeIfAbsent(api.getGroupName(), k -> new HashMap<>());
            map.putIfAbsent(api.getPattern(), objectNode);
        });
    }

    @Override
    public void skipApi(Map<String, String> skipApi) {
        List<ObjectNode> apis = sqlExecutorHolder.dataSource().list("select * from semi_api_auth");
        Map<String, String> apiMap = apis.stream().collect(Collectors.toMap(api -> api.path("pattern").asText(), api -> api.path("method").asText()));
        skipApi.putAll(apiMap);
    }
}
