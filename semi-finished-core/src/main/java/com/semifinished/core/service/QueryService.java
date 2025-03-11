package com.semifinished.core.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;

import java.util.List;

public interface QueryService {

    /**
     * 执行通用查询逻辑
     *
     * @param params 前端传入的结果
     * @return 返回前端的结果
     */
    Object commonQuery(ObjectNode params);

    /**
     * 执行通用查询逻辑
     *
     * @param sqlDefinition SQL定义信息
     * @return 返回前端的结果
     */
    Object commonQuery(SqlDefinition sqlDefinition);

    /**
     * 返回支持增强的增强器
     *
     * @param sqlDefinition SQL定义信息
     * @return 支持增强的增强器
     */
    List<AfterQueryEnhance> supportEnhances(SqlDefinition sqlDefinition);

    /**
     * 在解析SQL定义之前执行预处理逻辑
     *
     * @param sqlDefinition SQL定义对象，包含待解析的SQL语句及其元数据
     * @param enhances      后置查询增强器列表
     */
    void beforeParse(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances);

    /**
     * 执行SQL定义的核心解析逻辑
     *
     * @param sqlDefinition SQL定义信息
     */
    void parse(SqlDefinition sqlDefinition);

    /**
     * 在完成SQL定义解析后执行后处理逻辑
     *
     * @param sqlDefinition SQL定义信息
     * @param enhances      后置查询增强器列表
     */
    void afterParse(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances);

    /**
     * 执行查询并返回结果集
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询结果对象节点列表，每个节点代表一个结构化查询结果
     */
    List<ObjectNode> query(SqlDefinition sqlDefinition);

    /**
     * 在查询执行完成后处理结果集
     *
     * @param sqlDefinition SQL定义信息
     * @param enhances      后置查询增强器列表，用于对结果集进行扩展处理
     * @param resultHolder  包含分页(如果有)，结果集的容器
     */
    void afterQuery(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances, ResultHolder resultHolder);
}
