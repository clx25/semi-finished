package com.semifinished.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
@AllArgsConstructor
//@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final ObjectMapper objectMapper;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestValueResolver(objectMapper));
        resolvers.add(new RequestNodeResolver(objectMapper));
    }

}
