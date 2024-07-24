package com.semifinished.api.listener;

import com.semifinished.api.annotation.JsonApi;
import com.semifinished.core.config.CoreConfigurer;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//todo 添加注解方式的接口与组名对应

/**
 *
 */
@Order(-100)
@Component
@AllArgsConstructor
public class ApiConfigurerListener implements ApplicationListener<ContextRefreshedEvent> {
    private final JsonApiInit jsonApiInit;
    private final List<CoreConfigurer> coreConfigurers;
    private final ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Controller.class);

        beansWithAnnotation.values().stream()
                .map(Object::getClass)
                .flatMap(clazz -> Arrays.stream(ReflectionUtils.getAllDeclaredMethods(clazz)))
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, JsonApi.class))
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class))
                .forEach(method -> {
                    JsonApi jsonApi = AnnotatedElementUtils.getMergedAnnotation(method, JsonApi.class);
                    RequestMapping requestMapping = AnnotatedElementUtils.getMergedAnnotation(method, RequestMapping.class);
                    String name = requestMapping.name();
                    if (!StringUtils.hasText(name)) {
                        return;
                    }
                    jsonApiInit.getJsonConfig().put(name.toUpperCase(), jsonApi.group().toUpperCase());
                });

        if (CollectionUtils.isEmpty(coreConfigurers)) {
            return;
        }

        for (CoreConfigurer coreConfigurer : coreConfigurers) {
            coreConfigurer.addJsonConfig(jsonApiInit.getJsonConfig());
        }

    }
}
