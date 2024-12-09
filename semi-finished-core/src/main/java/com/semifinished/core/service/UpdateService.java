package com.semifinished.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.facotry.SqlDefinitionFactory;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.UpdateSqlCombiner;
import com.semifinished.core.jdbc.executor.Executor;
import com.semifinished.core.jdbc.parser.paramsParser.ParamsParser;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final List<ParamsParser> paramsParsers;
    private final List<AfterUpdateEnhance> afterUpdateEnhances;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SqlDefinitionFactory sqlDefinitionFactory;
    private final List<Executor> executorList;
    private final QueryService queryService;

    public void delete(ObjectNode params) {
        SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params.deepCopy());
        delete(sqlDefinition);
    }

    /**
     * 删除数据
     *
     * @param sqlDefinition SQL定义信息
     */
    public void delete(SqlDefinition sqlDefinition) {

        execute((definition -> executorList.stream()
                .filter(q -> definition.getDialect().equals(q.dialect()))
                .findFirst()
                .orElseThrow(() -> new CodeException("未找到对应执行器"))
                .delete(definition)), sqlDefinition);
    }

    public String add(JsonNode params) {
        SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params.deepCopy());
        add(sqlDefinition);
        return sqlDefinition.getId();
    }

    /**
     * 新增数据
     *
     * @param sqlDefinition SQL定义信息
     */
    public void add(SqlDefinition sqlDefinition) {

        JsonNode params = sqlDefinition.getParams();

        Consumer<SqlDefinition> consumer = params.has("@batch") ? (definition) -> {
            String sql = UpdateSqlCombiner.addSQLExcludeId(definition, configProperties.getIdKey());
            Map<String, Object>[] args = UpdateSqlCombiner.getBatchArgs(definition, objectMapper);
            sqlExecutorHolder.dataSource(definition.getDataSource())
                    .batchUpdate(sql, args);
        } : (definition) -> executorList.stream()
                .filter(q -> definition.getDialect().equals(q.dialect()))
                .findFirst()
                .orElseThrow(() -> new CodeException("未找到对应执行器"))
                .add(definition, configProperties.getIdKey());

        execute(consumer, sqlDefinition);
    }

    public void update(JsonNode params) {
        SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params.deepCopy());
        update(sqlDefinition);
    }

    /**
     * 修改数据
     *
     * @param sqlDefinition SQL定义信息
     */
    public void update(SqlDefinition sqlDefinition) {

        ObjectNode params = sqlDefinition.getParams();
        Consumer<SqlDefinition> consumer = params.has("@batch") ? (definition) -> {
            String sql = UpdateSqlCombiner.updateByIdSQL(definition, configProperties.getIdKey());
            Map<String, Object>[] args = UpdateSqlCombiner.getBatchArgs(definition, objectMapper);
            sqlExecutorHolder.dataSource(definition.getDataSource())
                    .batchUpdate(sql, args);
        } : (definition) -> executorList.stream()
                .filter(q -> definition.getDialect().equals(q.dialect()))
                .findFirst()
                .orElseThrow(() -> new CodeException("未找到对应执行器"))
                .update(definition);

        execute(consumer, sqlDefinition);
    }

//    /**
//     * 批量新增
//     *
//     * @param params 请求参数
//     */
//    public void batchAdd(JsonNode params) {
//        execute(params, (sqlDefinition) -> {
//            String sql = UpdateSqlCombiner.addSQLExcludeId(sqlDefinition, configProperties.getIdKey());
//
//            Map<String, Object>[] args = UpdateSqlCombiner.getBatchArgs(sqlDefinition, objectMapper);
//            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
//                    .batchUpdate(sql, args);
//        });
//    }

//    /**
//     * 批量修改
//     *
//     * @param params 请求参数
//     */
//    public void batchUpdate(JsonNode params) {
//        execute(params, (sqlDefinition) -> {
//            String sql = UpdateSqlCombiner.updateSQL(sqlDefinition, configProperties.getIdKey());
//            Map<String, Object>[] args = UpdateSqlCombiner.getBatchArgs(sqlDefinition, objectMapper);
//            sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
//                    .batchUpdate(sql, args);
//        });
//    }


//    private void execute(JsonNode params, Consumer<SqlDefinition> consumer) {
//
//        if (params instanceof ArrayNode) {
//            params = JsonNodeFactory.instance.objectNode().set("@batch", params);
//        }
//
//        SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params.deepCopy());
//
//
//        execute((ObjectNode) params, consumer, sqlDefinition);
//    }


    /**
     * 解析请求参数并根据解析内容执行SQL
     *
     * @param consumer      消费解析完成后的参数
     * @param sqlDefinition SQL定义信息
     */
    private void execute(Consumer<SqlDefinition> consumer, SqlDefinition sqlDefinition) {


        //筛选出支持本次请求的增强类
        List<AfterUpdateEnhance> afterUpdateEnhances = supportEnhance(sqlDefinition);

        afterUpdateEnhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));

        paramsParsers.forEach(parser -> parser.parse(sqlDefinition.getParams(), sqlDefinition));

        afterUpdateEnhances.forEach(enhance -> enhance.afterParse(sqlDefinition));

        consumer.accept(sqlDefinition);

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


    public void multi(JsonNode params) {
        sqlExecutorHolder.dataSource(null).transaction(executor -> {
            SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params);

            List<Pair<Integer, Runnable>> execute = new ArrayList<>();


            sqlDefinition.getParams().fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("%参数类型错误", key));


                SqlDefinition definition = new SqlDefinition();

                definition.setParams((ObjectNode) value);

                definition.setRawParams(params);

                //获取执行排序
                String[] keys = key.split(":");
                Assert.isFalse(keys.length == 2, () -> new ParamsException("参数%s错误", key));
                String index = keys[1];
                Assert.isFalse(ParamsUtils.isInteger(index), () -> new ParamsException("参数%s错误", key));


                switch (keys[0]) {
                    case "u":
                        execute.add(Pair.create(Integer.parseInt(index), () -> update(definition)));
                        break;
                    case "c":
                        //todo 需要完善 把上一个新增返回的id作为下一个新增的参数
                        execute.add(Pair.create(Integer.parseInt(index), () -> add(definition)));
                        break;
                    case "d":
                        execute.add(Pair.create(Integer.parseInt(index), () -> delete(definition)));
                        break;
                    case "r":
                        queryService.query(definition);
                }
            });

            execute.sort(Comparator.comparing(Pair::getKey));

            execute.forEach(p -> p.getValue().run());
        });
    }
}
