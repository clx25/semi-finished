package com.semifinished.core.jdbc.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@AllArgsConstructor
public class MySqlQuery implements Query {
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public String dialect() {
        return "mysql";
    }

    @Override
    public List<ObjectNode> query(SqlDefinition sqlDefinition) {
        //组装查询SQL并获取
        String sql = QuerySqlCombiner.query(sqlDefinition);

        //执行查询
        return sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .list(sql, QuerySqlCombiner.getArgs(sqlDefinition));

    }


    @Override
    public void add(SqlDefinition sqlDefinition) {

    }

    @Override
    public void update(SqlDefinition sqlDefinition) {

    }

    @Override
    public void delete(SqlDefinition sqlDefinition) {

    }


}
