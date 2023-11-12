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

/**
 * 排序规则解析
 * <pre>
 *     {
 *         "/":"col",
 *         "\\":"col"
 *     }
 * </pre>
 * "/":"col"-> order by col desc
 * "\\":"col" -> order by col
 * \反斜杠在json中是转义符，所以需要\\
 */

@Component
@AllArgsConstructor
public class SortParserSelect implements SelectParamsParser {
    private final SemiCache semiCache;
    private final CommonParser commonParser;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (sqlDefinition.getOrderFragment() != null) {
            return false;
        }
        String order = "";
        if ("/".equals(key)) {
            order = "desc";
        } else if ("\\".equals(key)) {
            order = "asc";
        } else {
            return false;
        }
        String[] columns = value.asText().split(",");
        String[] orders = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, columns[i]);
            orders[i] = columns[i] + " " + order;
        }

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, columns);

        sqlDefinition.setOrderFragment(String.join(",", orders));

        return true;
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
