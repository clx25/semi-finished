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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 批量处理接口参数系解析
 */
@Component
@AllArgsConstructor
public class BatchParser implements ParamsParser {
    private final TableUtils tableUtils;
    private final CommonParser commonParser;

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

        String table = sqlDefinition.getTable();
        //校验字段
        tableUtils.validColumnsName(sqlDefinition, table, columns);
        Map<String, String> columnMap = columns.stream().collect(Collectors.toMap(column -> column, column -> commonParser.getActualColumn(sqlDefinition.getDataSource(), table, column)));

        //补全有缺失的字段
        batch.forEach(node -> {
            ObjectNode n = (ObjectNode) node;
            columns.stream()
                    .filter(column -> !node.has(column))
                    .peek(name -> n.put(name, (String) null))
                    .forEach(name -> {
                        String actualColumn = columnMap.get(name);
                        JsonNode value = n.remove(name);
                        n.set(actualColumn, value);
                    });
        });

        sqlDefinition.getExpand().set("@batch", batch);
        params.setAll((ObjectNode) batch.get(0));
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
