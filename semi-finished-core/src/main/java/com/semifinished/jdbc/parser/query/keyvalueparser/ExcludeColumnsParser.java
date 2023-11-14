package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.config.DataSourceConfig;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.jdbc.parser.query.ParamsParser;
import com.semifinished.util.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 在SQL层面排除字段
 * <pre>
 *     {
 *         "~":"col1,col2"
 *     }
 * </pre>
 * 以上规则的意思是排除col1和col2字段，只查询其他字段
 */
@Component
@AllArgsConstructor
public class ExcludeColumnsParser implements ParamsParser {

    private final SemiCache semiCache;
    private final CommonParser commonParser;
    private final DataSourceConfig dataSourceConfig;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        String table = sqlDefinition.getTable();
        excludesConfig(table, sqlDefinition);

        JsonNode value = params.remove("~");
        if (value == null) {
            return;
        }

        String[] fields = value.asText().split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, fields[i]);
        }

        TableUtils.validColumnsName(semiCache, sqlDefinition, table, fields);

        sqlDefinition.addExcludeColumns(table, fields);
    }

    private void excludesConfig(String table, SqlDefinition sqlDefinition) {
        Map<String, List<String>> excludes = dataSourceConfig.getExcludes();
        if (excludes == null || excludes.isEmpty()) {
            return;
        }
        List<String> columns = excludes.get(table);
        if (columns == null || columns.isEmpty()) {
            return;
        }
        sqlDefinition.addExcludeColumns(table, columns);
    }

    @Override
    public int getOrder() {
        return -1000;
    }

}
