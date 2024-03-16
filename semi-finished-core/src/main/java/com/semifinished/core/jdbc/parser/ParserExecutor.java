package com.semifinished.core.jdbc.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.ParamsParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * sql解析的执行器
 * 并执行统一的前期处理
 */
@Component
public class ParserExecutor {

    @Resource
    private List<ParamsParser> paramsParsers;


    /**
     * 解析请求参数并返回SQL的合并器
     *
     * @param sqlDefinition SQL定义信息
     */
    public void parse(SqlDefinition sqlDefinition) {
        doParse(sqlDefinition.getParams(), paramsParsers, sqlDefinition);
    }

    /**
     * 解析请求参数
     * 该方法主要用于添加默认解析器
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        doParse(params, paramsParsers, sqlDefinition);
    }

    /**
     * 解析请求信息,创建SQL定义信息对象，并添加默认解析器
     *
     * @param params 请求参数
     * @return SQL定义信息
     */
    public SqlDefinition parse(ObjectNode params) {
        return parse(params, paramsParsers);
    }

    /**
     * 解析请求信息,创建SQL定义信息对象
     *
     * @param params        请求参数
     * @param paramsParsers 参数解析器集合
     * @return SQL定义信息
     */
    public SqlDefinition parse(ObjectNode params, Collection<? extends ParamsParser> paramsParsers) {
        SqlDefinition sqlDefinition = new SqlDefinition(params);
        doParse(params, paramsParsers, sqlDefinition);
        return sqlDefinition;
    }

    /**
     * 调用解析器解析请求信息
     *
     * @param params        请求参数
     * @param paramsParsers 参数解析器集合
     * @param sqlDefinition SQL定义信息
     */
    public void doParse(ObjectNode params, Collection<? extends ParamsParser> paramsParsers, SqlDefinition sqlDefinition) {
        for (ParamsParser paramsParser : paramsParsers) {
            paramsParser.parse(params, sqlDefinition);
        }
    }


}
