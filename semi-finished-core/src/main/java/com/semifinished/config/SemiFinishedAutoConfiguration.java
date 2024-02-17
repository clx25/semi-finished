package com.semifinished.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.semifinished.cache.CaffeineSemiCache;
import com.semifinished.cache.SemiCache;
import com.semifinished.exception.ConfigException;
import com.semifinished.jdbc.SqlExecutor;
import com.semifinished.util.Assert;
import com.semifinished.util.bean.SpringBeanUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 项目的自动配置类
 */
@Configuration
@AllArgsConstructor
@ComponentScan(basePackages = "com.semifinished")
public class SemiFinishedAutoConfiguration implements InitializingBean {

    private final SpringBeanUtils springBeanUtils;


    @Bean
    @ConditionalOnMissingBean
    public Cache<Object, Object> cache() {
        return Caffeine.newBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(SemiCache.class)
    public SemiCache semiCache() {
        return new CaffeineSemiCache();
    }

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties, ConfigProperties configProperties) {
        Map<String, ? extends HikariDataSource> dataSourceMap = dataSourceProperties.getDataSource();
        Assert.isNull(dataSourceMap, () -> new ConfigException("未配置数据源"));
        Assert.hasNotText(configProperties.getDataSource(), () -> new ConfigException("未指定默认数据源"));
        HikariDataSource dataSource = dataSourceMap.get(configProperties.getDataSource());
        Assert.isNull(dataSource, () -> new ConfigException("默认数据源" + configProperties.getDataSource() + "不存在"));
        return dataSource;
    }


    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(8, 8,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    @Autowired
    public void sqlExecutor(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                            TransactionTemplate transactionTemplate, ConfigProperties configProperties) {
        //注册SQLExecutor
        springBeanUtils.registerBean(SqlExecutor.class, () -> new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate), "sqlExecutor" + configProperties.getDataSource());
    }


    @Override
    public void afterPropertiesSet() {
        DataSourceProperties dataSourceProperties = springBeanUtils.getBean(DataSourceProperties.class);
        ConfigProperties configProperties = springBeanUtils.getBean(ConfigProperties.class);
        Map<String, ? extends HikariDataSource> dataSourceMap = dataSourceProperties.getDataSource();

        Assert.isTrue(dataSourceMap == null || dataSourceMap.isEmpty(), () -> new ConfigException("未添加数据库配置"));
        for (Map.Entry<String, ? extends HikariDataSource> entry : dataSourceMap.entrySet()) {
            String name = entry.getKey();
            if (name.equals(configProperties.getDataSource())) {
                continue;
            }

            HikariDataSource dataSource = entry.getValue();

            //注册dataSource
            springBeanUtils.registerBean(HikariDataSource.class, () -> dataSource, "dataSource" + name);

            //注册NamedParameterJdbcTemplate
            NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            springBeanUtils.registerBean(NamedParameterJdbcTemplate.class, () -> namedParameterJdbcTemplate, "namedParameterJdbcTemplate" + name);

            //注册TransactionTemplate
            TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
            springBeanUtils.registerBean(TransactionTemplate.class, () -> transactionTemplate, "transactionTemplate" + name);

            //注册SQLExecutor
            springBeanUtils.registerBean(SqlExecutor.class, () -> new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate), "sqlExecutor" + name);
        }


    }


}
