package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class GroupByParser implements SelectParamsParser {
    private final SemiCache semiCache;
    private final CommonParser commonParser;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"@group".equals(key)) {
            return false;
        }
        String columns = value.asText();
        String[] columnArray = columns.split(",");
        for (int i = 0; i < columnArray.length; i++) {
            columnArray[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, columnArray[i]);
        }
        TableUtils.validColumnsName(semiCache, sqlDefinition, table, columnArray);

        sqlDefinition.addGroupBy(table, columnArray);

        return true;
    }

    @Override
    public int getOrder() {
        return -700;
    }
}
