package com.semifinished.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;

import java.util.List;
import java.util.function.Consumer;

public interface UpdateService {

    String add(JsonNode params);

    void delete(ObjectNode params);

    void update(JsonNode params);

    /**
     * 在解析参数之前执行增强逻辑
     *
     * @param sqlDefinition       SQL定义信息
     * @param afterUpdateEnhances 增强类列表
     */
    void beforeParse(SqlDefinition sqlDefinition, List<AfterUpdateEnhance> afterUpdateEnhances);

    /**
     * 解析SQL定义中的参数
     *
     * @param sqlDefinition SQL定义信息
     */
    void parse(SqlDefinition sqlDefinition);

    /**
     * 在解析参数之后执行增强逻辑
     *
     * @param sqlDefinition       SQL定义信息
     * @param afterUpdateEnhances 增强类列表
     */
    void afterParse(SqlDefinition sqlDefinition, List<AfterUpdateEnhance> afterUpdateEnhances);

    /**
     * 解析请求参数并根据解析内容执行SQL
     *
     * @param consumer      消费解析完成后的参数
     * @param sqlDefinition SQL定义信息
     */
    void execute(Consumer<SqlDefinition> consumer, SqlDefinition sqlDefinition);

    /**
     * 在SQL执行完成后执行增强逻辑
     *
     * @param sqlDefinition       SQL定义信息
     * @param afterUpdateEnhances 增强类列表
     */
    void afterExecute(SqlDefinition sqlDefinition, List<AfterUpdateEnhance> afterUpdateEnhances);
}