package com.semifinished.listener;


import com.semifinished.config.ConfigProperties;
import com.semifinished.config.CoreConfigurer;
import com.semifinished.service.enhance.impl.DesensitizeEnhance;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
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
    private final ConfigProperties configProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (CollectionUtils.isEmpty(coreConfigurers)) {
            return;
        }

//        for (CoreConfigurer coreConfigurer : coreConfigurers) {
//
//            coreConfigurer.addTableMapping(getOrEmpty(getOrEmpty(configProperties.getMapping()).getTable()));
//
//
//            Map<String, List<String>> excludeColumn = new HashMap<>();
//            Map<String, List<String>> configExcludeColumn = getOrEmpty(configProperties.getExcludes());
//            coreConfigurer.addExcludeColumn(excludeColumn);
//            for (String key : excludeColumn.keySet()) {
//                List<String> value = configExcludeColumn.get(key);
//                if (value == null) {
//                    value = new ArrayList<>();
//                }
//                value.addAll(excludeColumn.get(key));
//                configExcludeColumn.put(key, value);
//            }
//
//
//            Map<String, Map<String, String>> columnMapping = new HashMap<>();
//            coreConfigurer.addColumnMapping(columnMapping);
//            Map<String, Map<String, String>> configColumnMapping = getOrEmpty(getOrEmpty(configProperties.getMapping()).getColumn());
//            for (String key : columnMapping.keySet()) {
//                Map<String, String> value = configColumnMapping.get(key);
//                if (value == null) {
//                    value = new HashMap<>();
//                }
//                value.putAll(columnMapping.get(key));
//                configColumnMapping.put(key, value);
//            }
//
//            String db = coreConfigurer.chooseDb();
//            if (StringUtils.hasText(db)) {
//                configProperties.setDataSource(db);
//            }
//
//            coreConfigurer.addDesensitize(desensitizeEnhance.getDesensitizes());
//        }
    }

    private <S> S getOrEmpty(S config) {
        return config == null ? (S) new HashMap<>() : config;
    }


}

