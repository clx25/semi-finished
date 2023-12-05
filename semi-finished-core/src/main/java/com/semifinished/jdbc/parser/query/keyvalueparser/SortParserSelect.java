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

    private final CommonParser commonParser;
    private final TableUtils tableUtils;

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
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.JOIN), () -> new ParamsException("排序规则位置错误"));
        String[] columns = value.asText().split(",");
        String[] orders = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = commonParser.getActualColumn(sqlDefinition.getDataSource(), table, columns[i]);
            orders[i] = columns[i] + " " + order;
        }

        tableUtils.validColumnsName(sqlDefinition, table, columns);

        sqlDefinition.setOrderFragment(String.join(",", orders));

        return true;
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
