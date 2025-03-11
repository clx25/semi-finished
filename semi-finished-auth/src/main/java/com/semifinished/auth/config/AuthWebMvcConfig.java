package com.semifinished.auth.config;

import com.semifinished.auth.interceptor.AuthenticationInterceptor;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
// @EnableWebMvc
public class AuthWebMvcConfig implements WebMvcConfigurer {
    private final AuthProperties authProperties;
    private final ConfigProperties configProperties;
    private final SemiCache semiCache;
    private final List<AuthConfigurer> authConfigurers;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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

        registry.addInterceptor(new AuthenticationInterceptor(configProperties, authProperties, skip));
    }


}
