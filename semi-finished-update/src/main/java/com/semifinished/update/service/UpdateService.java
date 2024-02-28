package com.semifinished.update.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.update.jdbc.UpdateSqlCombiner;
import com.semifinished.update.service.enhance.AfterUpdateEnhance;
import com.semifinished.core.jdbc.parser.query.ParamsParser;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class UpdateService {

    private final List<ParamsParser> paramsParsers;
    private final List<AfterUpdateEnhance> afterUpdateEnhances;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;

    /**
     * 修改数据
     *
     * @param params 请求参数
     */
    public void update(ObjectNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.updateSQL(sqlDefinition, configProperties.getIdKey());
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });
    }

    /**
     * 根据id删除数据
     *
     * @param table 表名
     * @param id    主键数据
     */
    public void delete(String table, String id) {
        ObjectNode params = JsonNodeFactory.instance.objectNode()
                .put(configProperties.getIdKey(), id)
                .put("@tb", table);
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, configProperties.getIdKey());
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });
    }

    /**
     * 根据id删除数据
     *
     * @param params 请求参数
     */
    public void delete(ObjectNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, configProperties.getIdKey());
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });
    }

    /**
     * 新增数据
     *
     * @param params 请求参数
     */
    public void add(ObjectNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.addSQLExcludeId(sqlDefinition, configProperties.getIdKey());
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });

    }

    /**
     * 解析请求参数并根据解析内容执行SQL
     *
     * @param params   请求参数
     * @param consumer 执行不同的SQL
     */
    private void execute(ObjectNode params, Consumer<SqlDefinition> consumer) {
        Assert.isEmpty(params, () -> new ParamsException("参数不能为空"));

        SqlDefinition sqlDefinition = new SqlDefinition(params);

        afterUpdateEnhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));

        paramsParsers.forEach(parser -> parser.parse(params, sqlDefinition));

        afterUpdateEnhances.forEach(enhance -> enhance.afterParse(sqlDefinition));


        sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .transaction(executor -> {

                    List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
                    Assert.isEmpty(valueConditions, () -> new ParamsException("参数不能为空"));
                    long count = valueConditions.stream().filter(v -> v.getCondition().startsWith("=:") || v.getCondition().trim().equals("is null")).count();
                    Assert.isFalse(count > 0, () -> new ParamsException("参数不能为空"));

                    Assert.hasNotText(sqlDefinition.getTable(), () -> new ParamsException("未指定表名"));

                    AfterUpdateEnhance.SqlAutoExecutor sqlAutoExecutor = new AfterUpdateEnhance.SqlAutoExecutor(consumer, sqlDefinition);


                    for (AfterUpdateEnhance enhance : afterUpdateEnhances) {
                        enhance.transactional(sqlAutoExecutor, sqlDefinition);
                    }
                    sqlAutoExecutor.exec();

                });

        afterUpdateEnhances.forEach(enhance -> enhance.afterExecute(sqlDefinition));
    }


}
