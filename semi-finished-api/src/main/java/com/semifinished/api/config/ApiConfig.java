package com.semifinished.api.config;

import com.semifinished.api.factory.ApiSqlDefinitionFactory;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.facotry.SqlDefinitionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ApiConfig {

//    @Bean
//    public SqlDefinitionFactory sqlDefinitionFactory(SemiCache semiCache) {
//        return new ApiSqlDefinitionFactory(semiCache);
//    }
}
