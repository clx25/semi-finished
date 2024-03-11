package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.semifinished.core.jdbc.parser.query.CommonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParserUtils;
import com.semifinished.core.utils.bean.TableUtils;
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
public class SortParser implements KeyValueParamsParser {

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
        Assert.isFalse(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.JOIN, ParserStatus.SUB_TABLE), () -> new ParamsException("排序规则位置错误"));
        String text = value.asText(null);
        Assert.hasNotText(text, () -> new ParamsException("排序规则字段不能为空：" + key));
        String[] columns = text.split(",");
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
