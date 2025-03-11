package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.config.DataSourceProperties;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParserUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 指定数据源
 * <pre>
 *     "@db":"database"
 * </pre>
 */

@Component
@AllArgsConstructor
public class DataSourceParser implements ParamsParser {
    private final ConfigProperties configProperties;
    private final DataSourceProperties dataSourceProperties;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {

        JsonNode db = params.remove("@db");

        if (!ParserUtils.statusAnyMatch(sqlDefinition, ParserStatus.NORMAL, ParserStatus.JOIN, ParserStatus.SUB_TABLE, ParserStatus.DICTIONARY)) {
            Assert.isFalse(db != null, () -> new ParamsException("数据源规则位置错误"));
            return;
        }

        String dataSource = "";
        if (db != null) {
            dataSource = db.asText(null);
            Assert.notBlank(dataSource, () -> new ParamsException("数据源规则不能为空"));
            dataSource = ParamsUtils.hasText(dataSource, configProperties.getDataSource());
        } else {
            if (StringUtils.hasText(sqlDefinition.getDataSource())) {
                return;
            }
            dataSource = configProperties.getDataSource();
        }

        String finalDataSource = dataSource;
        Assert.isTrue(dataSourceProperties.getDataSource().containsKey(dataSource), () -> new ParamsException("数据源" + finalDataSource + "不存在"));
        sqlDefinition.setDataSource(dataSource);
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
