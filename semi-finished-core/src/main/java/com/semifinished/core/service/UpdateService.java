package com.semifinished.core.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.UpdateSqlCombiner;
import com.semifinished.core.jdbc.parser.query.ParamsParser;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        String idKey = configProperties.getIdKey();
        ObjectNode params = JsonNodeFactory.instance.objectNode()
                .put(idKey, id)
                .put("@tb", table);
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, idKey);
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


        List<AfterUpdateEnhance> afterUpdateEnhances = supportEnhance(sqlDefinition);

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


    /**
     * 执行增强中的support方法，筛选支持此次请求的增强
     *
     * @param sqlDefinition SQL定义信息
     * @return 支持此次请求的增强方法
     */
    private List<AfterUpdateEnhance> supportEnhance(SqlDefinition sqlDefinition) {
        return this.afterUpdateEnhances.stream()
                .filter(enhance -> enhance.support(sqlDefinition))
                .collect(Collectors.toList());
    }


}
