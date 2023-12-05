package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.config.DataSourceConfig;
import com.semifinished.constant.ParserStatus;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.util.Assert;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.bean.TableUtils;
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
public class ExcludeColumnsParser implements SelectParamsParser {

    private final TableUtils tableUtils;
    private final CommonParser commonParser;
    private final DataSourceConfig dataSourceConfig;

    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        if (!"~".equals(key)) {
            return false;
        }
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE,
                ParserStatus.JOIN), () -> new ParamsException("排除规则位置错误"));

        excludesConfig(table, sqlDefinition);

        String[] fields = value.asText().split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, fields[i]);
        }

        tableUtils.validColumnsName(sqlDefinition, table, fields);

        sqlDefinition.addExcludeColumns(table, fields);
        return true;
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
