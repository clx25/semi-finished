package com.semifinished.web.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.config.ApiConfigurer;
import com.semifinished.api.excetion.ApiException;
import com.semifinished.auth.config.AuthConfigurer;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.web.pojo.Api;
import com.semifinished.web.pojo.ApiAuth;
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

    private final ObjectMapper objectMapper;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void addApiMap(Map<String, Map<String, ObjectNode>> apiMap) {

        List<Api> semiApis = sqlExecutorHolder.dataSource().list("select * from semi_api", Api.class);
        semiApis.stream().filter(api->!api.isDisabled()).filter(api -> {
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
            ObjectNode params;
            try {
                params = objectMapper.readValue(api.getParams(), ObjectNode.class);
            } catch (JsonProcessingException e) {
                throw new ApiException("接口配置参数错误",e);
            }
            objectNode.put("summary", api.getSummary());
            objectNode.put("version", api.getVersion());
            objectNode.set("params", params);
            objectNode.put("config", api.getConfig());
            Map<String, ObjectNode> map = apiMap.computeIfAbsent(api.getGroupName(), k -> new HashMap<>());
            map.putIfAbsent(api.getPattern(), objectNode);
        });
    }

    @Override
    public void skipApi(Map<String, String> skipApi) {
        List<ApiAuth> apis = sqlExecutorHolder.dataSource().list("select * from semi_api_auth", ApiAuth.class);
        Map<String, String> apiMap = apis.stream().filter(auth->!auth.isDisabled()).collect(Collectors.toMap(ApiAuth::getPattern, ApiAuth::getMethod));
        skipApi.putAll(apiMap);
    }
}
