package com.semifinished.listener;


import com.semifinished.config.ConfigProperties;
import com.semifinished.config.CoreConfigurer;
import com.semifinished.config.DataSourceConfig;
import com.semifinished.config.DataSourceProperties;
import com.semifinished.service.enhance.query.DesensitizeEnhance;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把代码配置放入缓存
 */
@Component
@Order(-500)
@RequiredArgsConstructor
public class CacheConfigurerListener implements ApplicationListener<ContextRefreshedEvent> {

    private final List<CoreConfigurer> coreConfigurers;
    private final DesensitizeEnhance desensitizeEnhance;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (CollectionUtils.isEmpty(coreConfigurers)) {
            return;
        }

        for (CoreConfigurer coreConfigurer : coreConfigurers) {
            coreConfigurer.addDesensitize(desensitizeEnhance.getDesensitizes());
        }
    }

//    private <S> S getOrEmpty(S config) {
//        return config == null ? (S) new HashMap<>() : config;
//    }


}

