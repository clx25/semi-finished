package com.semifinished.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Component
@AllArgsConstructor
public class ApiWebMvcConfig implements WebMvcConfigurer {
    private final ObjectMapper objectMapper;
    private final ApiProperties apiProperties;
//    private final ParamsReplacer paramsReplacer;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        resolvers.add(new ApiParamHandlerMethodArgumentResolver(objectMapper, apiProperties, paramsReplacer));
    }

}
