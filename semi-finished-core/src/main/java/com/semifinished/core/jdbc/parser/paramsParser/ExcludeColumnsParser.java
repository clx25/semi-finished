package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.DataSourceConfig;
import com.semifinished.core.config.DataSourceProperties;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

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

    private final TableUtils tableUtils;
    private final CommonParser commonParser;
    private final DataSourceProperties dataSourceProperties;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        List<Column> columns = sqlDefinition.getColumns();
        String table = sqlDefinition.getTable();
        Set<String> excludes = excludesConfig(sqlDefinition.getDataSource(), table);
        sqlDefinition.addExcludeColumns(table, excludes);


        JsonNode value = params.path("~");
        if (value.isMissingNode()) {
            return;
        }


        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE,
                ParserStatus.JOIN, ParserStatus.DICTIONARY), () -> new ParamsException("排除规则位置错误"));

        if (columns == null || columns.isEmpty()) {
            return;
        }
        String text = value.asText();
        Assert.hasNotText(text, () -> new ParamsException("排除规则字段不能为空：~"));
        String[] fields = text.split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, fields[i].trim());
        }

        tableUtils.validColumnsName(sqlDefinition, table, fields);

        setDisabled(columns, Arrays.asList(fields), table);

    }

    /**
     * 设置禁止查询
     *
     * @param columns  查询字段
     * @param excludes 排除字段
     * @param table    表名
     */
    private void setDisabled(List<Column> columns, Collection<String> excludes, String table) {
        excludes.forEach(field -> columns.stream()
                .filter(column -> table.equals(column.getTable()))
                .filter(column -> field.equals(column.getColumn()))
                .forEach(column -> column.setDisabled(true))
        );
    }

    /**
     * 获取配置中指定表的排除字段
     *
     * @param datasource 数据源
     * @param table      表名
     * @return 指定表名的排除字段
     */
    private Set<String> excludesConfig(String datasource, String table) {
        DataSourceConfig dataSourceConfig = dataSourceProperties.getDataSource().get(datasource);
        Map<String, Set<String>> excludes = dataSourceConfig.getExcludes();
        if (excludes == null || excludes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> columns = excludes.get(table);

        return columns == null ? Collections.emptySet() : columns;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
