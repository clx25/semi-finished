package com.semifinished.jdbc;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 保存SQL的连接
 */
@Service
@AllArgsConstructor
public class SqlExecutorHolder {

    private final Map<String, SqlExecutor> sqlExecutorMap;


    public SqlExecutor dataSource(String name) {
        return sqlExecutorMap.get("sqlExecutor" + (name == null ? "" : StringUtils.capitalize(name)));
    }


}
