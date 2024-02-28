package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.utils.ParamsUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        //匹配出真正的排除字段，避免字段名重复导致的误排除
        //此逻辑建立在排除规则中，没有表名的一定是直接使用别名的前提下
        List<Column> excludeColumns = QuerySqlCombiner.excludeColumns(sqlDefinition);
        List<Column> queryColumns = QuerySqlCombiner.columnsAll(sqlDefinition);

        List<String> columns = new ArrayList<>();

        for (Column exclude : excludeColumns) {
            for (Column query : queryColumns) {
                if (!StringUtils.hasText(exclude.getTable())) {
                    if (exclude.getColumn().equals(query.getAlias())) {
                        columns.add(query.getAlias());
                    }
                } else if (exclude.getTable().equals(query.getTable()) && exclude.getColumn().equals(query.getColumn())) {
                    columns.add(ParamsUtils.hasText(query.getAlias(), query.getColumn()));
                }
            }
        }

        if (columns.isEmpty()) {
            return;
        }

        for (ObjectNode record : records) {
            removeValue(record, columns);
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
