package com.semifinished.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.QuerySqlCombiner;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排除字段
 */
@Order(1000)
@Component
public class ExcludeEnhance implements AfterQueryEnhance {


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        if (records.isEmpty()) {
            return;
        }

        List<String> excludeColumns = QuerySqlCombiner.excludeColumns(sqlDefinition).stream()
                .map(Column::getColumn).collect(Collectors.toList());

        for (ObjectNode record : records) {
            removeValue(record, excludeColumns);
        }

    }

    /**
     * 排除字段
     *
     * @param record         查询的一行数据
     * @param excludeColumns 排除的字段名集合
     */
    private static void removeValue(JsonNode record, List<String> excludeColumns) {
        for (String excludeColumn : excludeColumns) {
            Iterator<Map.Entry<String, JsonNode>> fields = record.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                if (excludeColumn.equals(next.getKey())) {
                    fields.remove();
                    continue;
                }

                JsonNode value = next.getValue();
                if (value instanceof ArrayNode) {
                    for (JsonNode jsonNode : value) {
                        removeValue(jsonNode, excludeColumns);
                    }
                }
            }


        }
    }

}
