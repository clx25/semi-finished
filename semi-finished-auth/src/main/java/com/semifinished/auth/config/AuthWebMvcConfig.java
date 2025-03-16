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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor(configProperties, authProperties, semiCache));
    }


}
