package com.semifinished.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties("spring")
public class DataSourceProperties {
    /**
     * 多数据源配置
     */
    private Map<String, DataSourceConfig> dataSource;
}
