package com.semifinished.core.jdbc;

import com.semifinished.core.config.ConfigProperties;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 保存SQL的连接
 */

@Getter
public class SqlExecutorHolder {

    private final Map<String, SqlExecutor> sqlExecutorMap;
    private final ConfigProperties configProperties;

    public SqlExecutorHolder(Map<String, SqlExecutor> sqlExecutorMap, ConfigProperties configProperties) {
        this.sqlExecutorMap = sqlExecutorMap;
        this.configProperties = configProperties;
    }

    public SqlExecutor dataSource(){
        return dataSource(null);
    }
    public SqlExecutor dataSource(String name) {
        if (!StringUtils.hasText(name)) {
            name = configProperties.getDataSource();
        }
        return sqlExecutorMap.get(name);
    }


}
