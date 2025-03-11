package com.semifinished.core.jdbc.parser.paramsParser.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
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
public class RowNumParser implements KeyValueParamsParser {
    @Override
    public boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition) {

        if (!"@row".equals(key)) {
            return false;
        }

        Assert.isTrue(ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.DICTIONARY),
                () -> new ParamsException("@row规则位置错误"));


        Assert.isFalse(sqlDefinition.isPage(), () -> new ParamsException("@row规则与分页规则冲突"));
        String text = value.asText(null);
        Assert.notBlank(text, () -> new ParamsException("@row规则值不能为空：" + key));
        String[] values = text.split(",");
        Assert.isFalse(values.length > 2, () -> new ParamsException("@row参数错误，最多只能有两个值"));

        boolean range = values.length != 2;
        Assert.isTrue(ParamsUtils.isInteger(values[0]) && (range || ParamsUtils.isInteger(values[1])), () -> new ParamsException("@row值不是整数"));

        int rowStart = Integer.parseInt(values[0]);
        int rowEnd = range ? rowStart : Integer.parseInt(values[values.length - 1]);

        Assert.isFalse(rowStart < 0 || rowEnd < 0 || (rowEnd != 0 && rowStart > rowEnd), () -> new ParamsException(key + "参数错误"));

        sqlDefinition.setRowStart(rowStart);
        sqlDefinition.setRowEnd(rowEnd);

        return true;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
