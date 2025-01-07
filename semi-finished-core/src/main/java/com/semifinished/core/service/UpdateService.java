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


}
