package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 解析group by规则
 */
@Component
@AllArgsConstructor
public class GroupByParser implements KeyValueParamsParser {
    private final TableUtils tableUtils;
    private final CommonParser commonParser;


    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"@group".equals(key)) {
            return false;
        }

        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE, ParserStatus.JOIN), () -> new ParamsException("@group规则位置错误"));

        String columns = value.asText();
        Assert.hasNotText(columns, () -> new ParamsException("group规则字段不能为空：" + key));
        String[] columnArray = columns.split(",");
        for (int i = 0; i < columnArray.length; i++) {
            columnArray[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, columnArray[i]);
        }
        tableUtils.validColumnsName(sqlDefinition, table, columnArray);

        sqlDefinition.addGroupBy(table, columnArray);

        List<Column> columnsList = sqlDefinition.getColumns();
        if (columnsList == null || columnsList.isEmpty()) {
            return true;
        }

        boolean cover = columnsList.stream().filter(col -> table.equals(col.getTable()))
                .allMatch(col -> Arrays.stream(columnArray)
                        .anyMatch(field -> field.equals(col.getColumn())));


        if (sqlDefinition.getStatus() == ParserStatus.SUB_TABLE.getStatus()) {
            Assert.isFalse(cover, () -> new ParamsException("子查询中的group字段必须包含查询字段"));
        }

        return true;
    }

    @Override
    public int getOrder() {
        return -700;
    }
}
