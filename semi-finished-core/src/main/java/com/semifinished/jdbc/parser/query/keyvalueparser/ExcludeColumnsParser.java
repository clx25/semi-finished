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
 * 在SQL层面排除字段
 * <pre>
 *     {
 *         "~":"col1,col2"
 *     }
 * </pre>
 * 以上规则的意思是排除col1和col2字段，只查询其他字段
 */
@Order(-1000)
@Component
@AllArgsConstructor
public class ExcludeColumnsParser implements SelectParamsParser {

    private final SemiCache semiCache;
    private final CommonParser commonParser;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"~".equals(key)) {
            return false;
        }

        String[] fields = value.asText().split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = commonParser.getActualColumn(table, fields[i]);
        }

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, fields);

        sqlDefinition.addExcludeColumns(table, fields);
        return true;
    }
}
