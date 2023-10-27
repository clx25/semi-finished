package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.interpolation.Interpolation;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 执行插值规则
 * 先把所有插值规则解析完成后再执行解析
 * 避免互相影响的规则获取到未解析的值
 * <pre>
 *     {
 *         "col$":"xxx"
 *     }
 * </pre>
 */
@Component
@AllArgsConstructor
@Order(-2000)
public class InterpolationParser implements ParamsParser {
    private final List<Interpolation> interpolations;

    @Override
    public void parser(ObjectNode params, SqlDefinition sqlDefinition) {
        ObjectNode copyNode = params.deepCopy();
        String table = sqlDefinition.getTable();
        copyNode.fields().forEachRemaining(e -> {
                    String k = e.getKey();
                    JsonNode v = e.getValue();

                    //如果是$结尾，表示使用插值规则，需要获取实际值
                    if (k.endsWith("$")) {
                        k = k.substring(0, k.length() - 1);
                        v = interpolation(table, v.asText(), sqlDefinition);
                    }
                    params.remove(e.getKey());
                    params.set(k, v);
                }
        );
    }


    private JsonNode interpolation(String table, String key, SqlDefinition sqlDefinition) {
        for (Interpolation interpolation : interpolations) {
            if (interpolation.match(key)) {
                return interpolation.value(table, key, sqlDefinition);
            }
        }
        throw new ParamsException("未找到" + key + "对应的值");
    }
}


