package com.semifinished.auth.listener;

import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthConfigurer;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.listener.RefreshCacheApplication;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把部分配置数据放入缓存
 */
@Component
@Order(200)
@AllArgsConstructor
public class AuthListener implements ApplicationListener<RefreshCacheApplication> {

    private final AuthProperties authProperties;
    private final List<AuthConfigurer> authConfigurers;
    private final SemiCache semiCache;

    @Override
    public void onApplicationEvent(RefreshCacheApplication event) {
        Map<String, String> skip = new HashMap<>();
        // 内置的跳过登录验证的接口
        skip.put("/login", "post");
        skip.put("/signup", "post");
        skip.put("/captcha", "get");
        skip.put("/error", "*");

        Map<String, String> propertiesSkip = authProperties.getSkip();
        if (!CollectionUtils.isEmpty(propertiesSkip)) {
            skip.putAll(propertiesSkip);
        }

        for (AuthConfigurer authConfigurer : authConfigurers) {
            authConfigurer.skipApi(skip);
        }

        semiCache.initHashValue(AuthCacheKey.SKIP_AUTH.getKey(), skip);
    }
}
