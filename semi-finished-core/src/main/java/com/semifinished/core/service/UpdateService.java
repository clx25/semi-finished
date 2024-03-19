package com.semifinished.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.UpdateSqlCombiner;
import com.semifinished.core.jdbc.parser.paramsParser.ParamsParser;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UpdateService {

    private final List<ParamsParser> paramsParsers;
    private final List<AfterUpdateEnhance> afterUpdateEnhances;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final TableUtils tableUtils;

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
            String sql;
            if (configProperties.isLogicDelete()) {
                String logicDeleteColumn = configProperties.getLogicDeleteColumn();
                tableUtils.validColumnsName(sqlDefinition, sqlDefinition.getTable(), logicDeleteColumn);
                sql = UpdateSqlCombiner.logicDeleteSQL(sqlDefinition, idKey, logicDeleteColumn);
            } else {
                sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, idKey);
            }

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
     * 批量新增
     *
     * @param params 请求参数
     */
    public void batchAdd(JsonNode params) {

        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.addSQLExcludeId(sqlDefinition, configProperties.getIdKey());
            Map<String, Object>[] args = objectMapper.convertValue(sqlDefinition.getExpand().get("@batch"), new TypeReference<Map<String, Object>[]>() {
            });
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .batchUpdate(sql, args);
        });
    }

    /**
     * 批量修改
     *
     * @param params 请求参数
     */
    public void batchUpdate(JsonNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.updateSQL(sqlDefinition, configProperties.getIdKey());
            Map<String, Object>[] args = objectMapper.convertValue(sqlDefinition.getExpand().get("@batch"), new TypeReference<Map<String, Object>[]>() {
            });
            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                    .batchUpdate(sql, args);
        });
    }


    private void execute(JsonNode params, Consumer<SqlDefinition> consumer) {

        SqlDefinition sqlDefinition = new SqlDefinition();
        sqlDefinition.setRawParams(params);
        if (params instanceof ArrayNode) {
            params = JsonNodeFactory.instance.objectNode().set("@batch", params);
        }
        sqlDefinition.setParams((ObjectNode) params);

        execute((ObjectNode) params, consumer, sqlDefinition);
    }


    /**
     * 解析请求参数并根据解析内容执行SQL
     *
     * @param params        请求参数
     * @param consumer      消费解析完成后的参数
     * @param sqlDefinition SQL定义信息
     */
    private void execute(ObjectNode params, Consumer<SqlDefinition> consumer, SqlDefinition sqlDefinition) {
        Assert.isEmpty(params, () -> new ParamsException("参数不能为空"));

        //筛选出支持本次查询的增强类
        List<AfterUpdateEnhance> afterUpdateEnhances = supportEnhance(sqlDefinition);

        afterUpdateEnhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));

        paramsParsers.forEach(parser -> parser.parse(params, sqlDefinition));

        afterUpdateEnhances.forEach(enhance -> enhance.afterParse(sqlDefinition));

        //执行SQL语句
        sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .transaction(executor -> {

                    List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
                    Assert.isEmpty(valueConditions, () -> new ParamsException("参数不能为空"));

                    //过滤掉
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
