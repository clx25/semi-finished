package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;


/**
 * 请求参数解析器
 */
public interface ParamsParser {
    /**
     * 解析参数接口方法
     * 为了避免该方法解析过的参数在{@link KeyValueParamsParser}中再解析一次，
     * 所以需要把解析过的参数删掉
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    void parser(ObjectNode params, SqlDefinition sqlDefinition);
}
