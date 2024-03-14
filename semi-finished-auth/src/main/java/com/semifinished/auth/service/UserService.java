package com.semifinished.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.service.EnhanceService;
import com.semifinished.core.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserService {

    private final SemiCache semiCache;
    private final ConfigProperties configProperties;
    @Resource
    private EnhanceService enhanceService;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public ObjectNode getCurrent() {
        String idKey = configProperties.getIdKey();
        String userId = RequestUtils.getRequestAttributes(idKey);
        ObjectNode user = semiCache.getValue(AuthCacheKey.USER.getKey(), userId);

        if (user != null) {
            return user;
        }
        Map<String, JsonNode> apiMap = semiCache.getValue(CoreCacheKey.CUSTOM_API.getKey(), "POST");

        ObjectNode params = apiMap.getOrDefault("/login", JsonNodeFactory.instance.missingNode()).with("params");
        params.remove("@bean");
        params.remove("username$$");
        params.put(idKey, userId);

        user = (ObjectNode) enhanceService.query(params);
        semiCache.addValue(AuthCacheKey.USER.getKey(), userId, user);

        return user;

    }


}
