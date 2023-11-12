package com.semifinished.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 指定返回第几行数据，或范围
 * 与分页规则冲突
 * 序号从1开始
 * e.g.
 * <pre>
 *     {
 *         "pageSize":10,
 *         "pageNum":2,
 *         "@row":2
 *     }
 * </pre>
 * 上方的请求参数表示返回第二页的第二条数据
 */

@Component
public class RowNumParser implements SelectParamsParser {
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {
        if (!"@row".equals(key)) {
            return false;
        }
        Assert.isTrue(sqlDefinition.isPage(), () -> new ParamsException("@row规则与分页规则冲突"));
        String text = value.asText();
        String[] values = text.split(",");
        Assert.isTrue(values.length > 2, () -> new ParamsException("@row参数错误，最多只能有两个值"));

        boolean range = values.length != 2;
        Assert.isFalse(ParamsUtils.isInteger(values[0]) && (range || ParamsUtils.isInteger(values[1])), () -> new ParamsException("@row值不是整数"));

        int rowStart = Integer.parseInt(values[0]);
        int rowEnd = range ? 0 : Integer.parseInt(values[values.length - 1]);

        Assert.isTrue(rowStart != 0 && rowEnd != 0 && rowStart > rowEnd, () -> new ParamsException(key + "参数错误"));

        sqlDefinition.setRowStart(Math.max(rowStart, 1));
        sqlDefinition.setRowEnd(rowEnd);

        return true;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
