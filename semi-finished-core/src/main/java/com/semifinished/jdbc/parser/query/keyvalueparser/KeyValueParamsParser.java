package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import org.springframework.core.Ordered;

/**
 * 前端参数的解析器接口
 * 适用于匹配一个确定的key
 * 如果需要匹配一个需要解析的key,可以使用{@link SelectParamsParser}
 */
public interface KeyValueParamsParser extends Ordered {
    /**
     * 对前端传过来的json参数进行解析
     * 具体示例可查看实现类，如{@link LikeParamsParser}
     *
     * @param table         表名，在调用之前对table进行过校验，可以保证能从semicache中获取字段列表
     * @param key           前端json参数key
     * @param value         前端json参数value
     * @param sqlDefinition SQL定义信息
     * @return 是否使用了该解析器，如果为true,则该查询条件不再进行下一次解析
     */
    boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition);
}
