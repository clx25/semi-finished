package com.semifinished.core.jdbc.parser.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;
import org.springframework.core.Ordered;


/**
 * 请求参数解析器
 * 适用于匹配一个确定的key
 * 如果需要匹配一个需要解析的key,可以使用{@link KeyValueParamsParser}
 */
public interface ParamsParser extends Ordered {
    /**
     * 解析参数接口方法
     * 为了避免该方法解析过的参数在{@link KeyValueParamsParser}中再解析一次，
     * 所以需要把解析过的参数删掉
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    void parse(ObjectNode params, SqlDefinition sqlDefinition);
}
