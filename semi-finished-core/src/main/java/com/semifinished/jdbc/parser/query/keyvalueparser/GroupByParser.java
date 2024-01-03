package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.constant.ParserStatus;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.pojo.Column;
import com.semifinished.util.Assert;
import com.semifinished.util.ParserUtils;
import com.semifinished.util.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
@AllArgsConstructor
public class GroupByParser implements SelectParamsParser {
    private final TableUtils tableUtils;
    private final CommonParser commonParser;


    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"@group".equals(key)) {
            return false;
        }

        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.SUB_TABLE, ParserStatus.JOIN), () -> new ParamsException("@group规则位置错误"));

        String columns = value.asText();
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
