package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
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
        //todo subtable状态也可以，需要在增强类中afterParser后检查group字段是否包含查询字段
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL), () -> new ParamsException("@group规则位置错误"));

        String columns = value.asText();
        String[] columnArray = columns.split(",");
        for (int i = 0; i < columnArray.length; i++) {
            columnArray[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, columnArray[i]);
        }
        tableUtils.validColumnsName(sqlDefinition, table, columnArray);

        sqlDefinition.addGroupBy(table, columnArray);

        return true;
    }

    @Override
    public int getOrder() {
        return -700;
    }
}
