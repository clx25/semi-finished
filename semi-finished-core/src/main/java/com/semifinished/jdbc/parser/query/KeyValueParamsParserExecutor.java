package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 单个键值的解析
 */
@Order(500)
@Component
public class KeyValueParamsParserExecutor implements ParamsParser {

    @Resource
    private List<KeyValueParamsParser> keyValueParamsParsers;

    /**
     * 把请求参数拆分为键值去匹配解析器
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    @Override
    public void parser(ObjectNode params, SqlDefinition sqlDefinition) {
        parser(params, sqlDefinition, keyValueParamsParsers);
    }

    public void parser(ObjectNode params, SqlDefinition sqlDefinition, List<KeyValueParamsParser> keyValueParamsParsers) {
        // 解析器作为外循环用于控制解析器的生效顺序
        for (KeyValueParamsParser paramsParser : keyValueParamsParsers) {
            Iterator<Map.Entry<String, JsonNode>> iterator = params.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> node = iterator.next();
                if (paramsParser.parse(sqlDefinition.getTable(), node.getKey(), node.getValue(), sqlDefinition)) {
                    iterator.remove();
                }
            }
        }
    }
}
