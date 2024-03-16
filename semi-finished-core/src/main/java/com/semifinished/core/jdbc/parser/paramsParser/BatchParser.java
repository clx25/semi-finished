package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


/**
 * 批量处理接口参数系解析
 */
@Component
@AllArgsConstructor
public class BatchParser implements ParamsParser {
    private final TableUtils tableUtils;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        JsonNode batch = params.remove("@batch");
        if (batch == null) {
            return;
        }

        Assert.isFalse(batch instanceof ArrayNode, () -> new ParamsException("批量参数错误"));

        //获取参数所有存在的字段
        Set<String> columns = new HashSet<>();
        batch.forEach(node -> {
            Assert.isFalse(node instanceof ObjectNode, () -> new ParamsException("批量参数错误"));
            node.fieldNames().forEachRemaining(columns::add);
        });

        //校验字段
        tableUtils.validColumnsName(sqlDefinition, sqlDefinition.getTable(), columns);

        //补全有缺失的字段
        batch.forEach(node -> columns.stream()
                .filter(column -> !node.has(column))
                .forEach(name -> ((ObjectNode) node).put(name, (String) null))
        );

        sqlDefinition.getExpand().set("@batch", batch);
        params.setAll((ObjectNode) batch.get(0));
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
