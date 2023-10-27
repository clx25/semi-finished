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
import com.semifinished.util.SpringBeanUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 项目的自动配置类
 */
@Configuration
@AllArgsConstructor
@ComponentScan(basePackages = "com.semifinished")
public class SemiFinishedAutoConfiguration implements InitializingBean {

    private final DefaultListableBeanFactory defaultListableBeanFactory;


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
        HikariDataSource dataSource = dataSourceProperties.getDatasource().get(configProperties.getDatabase());
        Assert.isNull(dataSource, () -> new ConfigException(configProperties.getDatabase() + "数据源不存在"));
        return dataSource;
    }


    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(8, 8,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    @Bean
    public SqlExecutor sqlExecutor(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                   TransactionTemplate transactionTemplate) {
        return new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate);
    }


    @Override
    public void afterPropertiesSet() {
        DataSourceProperties dataSourceProperties = SpringBeanUtils.getBean(defaultListableBeanFactory, DataSourceProperties.class);
        ConfigProperties configProperties = SpringBeanUtils.getBean(defaultListableBeanFactory, ConfigProperties.class);
        Map<String, HikariDataSource> dataSources = dataSourceProperties.getDatasource();

        Assert.isTrue(dataSources == null || dataSources.isEmpty(), () -> new ConfigException("未添加数据库配置"));
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            String key = entry.getKey();
            if (key.equals(configProperties.getDatabase())) {
                continue;
            }

            String name = StringUtils.capitalize(key);
            HikariDataSource dataSource = entry.getValue();

            //注册dataSource
            registerBean(HikariDataSource.class, () -> dataSource, "dataSource" + name);

            //注册NamedParameterJdbcTemplate
            NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            registerBean(NamedParameterJdbcTemplate.class, () -> namedParameterJdbcTemplate, "namedParameterJdbcTemplate" + name);

            //注册TransactionTemplate
            TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
            registerBean(TransactionTemplate.class, () -> transactionTemplate, "transactionTemplate" + name);

            //注册SQLExecutor
            registerBean(SqlExecutor.class, () -> new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate), "sqlExecutor" + name);
        }
    }

    private <T> void registerBean(Class<T> clazz, Supplier<T> supplier, String name) {
        SpringBeanUtils.registerBean(defaultListableBeanFactory, clazz, supplier, name);
    }


}
