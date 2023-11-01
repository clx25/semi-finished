package com.semifinished.jdbc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 保存SQL的连接
 */
@Service
@Getter
@AllArgsConstructor
public class SqlExecutorHolder {

    private final Map<String, SqlExecutor> sqlExecutorMap;


    public SqlExecutor dataSource(String name) {
        return sqlExecutorMap.get("sqlExecutor" + (name == null ? "" : name));
    }


}
