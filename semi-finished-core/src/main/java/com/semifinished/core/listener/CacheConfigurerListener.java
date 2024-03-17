package com.semifinished.core.listener;


import com.semifinished.core.config.CoreConfigurer;
import com.semifinished.core.service.enhance.query.DesensitizeEnhance;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 把代码配置放入缓存
 */
@Component
@Order(-500)
@RequiredArgsConstructor
public class CacheConfigurerListener implements ApplicationListener<ContextRefreshedEvent> {

    private final List<CoreConfigurer> coreConfigurers;
    private final DesensitizeEnhance desensitizeEnhance;
    private final JsonConfigsInit jsonConfigsInit;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (CollectionUtils.isEmpty(coreConfigurers)) {
            return;
        }

        for (CoreConfigurer coreConfigurer : coreConfigurers) {
            coreConfigurer.addDesensitize(desensitizeEnhance.getDesensitizes());
            coreConfigurer.addJsonConfig(jsonConfigsInit.getJsonConfig());
        }

    }

}

