package com.semifinished.core.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.semifinished.core.cache.CaffeineSemiCache;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.facotry.DefaultSqlDefinitionFactory;
import com.semifinished.core.facotry.SqlDefinitionFactory;
import com.semifinished.core.jdbc.SqlExecutor;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.utils.Assert;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
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
public class SemiFinishedAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SqlDefinitionFactory sqlDefinitionFactory() {
        return new DefaultSqlDefinitionFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public Cache<Object, Object> cache() {
        return Caffeine.newBuilder().build();
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

    @Bean
    public SqlExecutorHolder sqlExecutorHolder(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                               DataSourceProperties dataSourceProperties,
                                               TransactionTemplate transactionTemplate,
                                               ConfigProperties configProperties) {

        Map<String, SqlExecutor> sqlExecutorMap = new HashMap<>();
        sqlExecutorMap.put(configProperties.getDataSource(), new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate));


        Map<String, ? extends HikariDataSource> dataSourceMap = dataSourceProperties.getDataSource();
        for (Map.Entry<String, ? extends HikariDataSource> entry : dataSourceMap.entrySet()) {
            String name = entry.getKey();
            if (name.equals(configProperties.getDataSource())) {
                continue;
            }

            HikariDataSource dataSource = entry.getValue();

            namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

            transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

            sqlExecutorMap.put(name, new SqlExecutor(namedParameterJdbcTemplate, transactionTemplate));
        }
        return new SqlExecutorHolder(sqlExecutorMap, configProperties);
    }


}
