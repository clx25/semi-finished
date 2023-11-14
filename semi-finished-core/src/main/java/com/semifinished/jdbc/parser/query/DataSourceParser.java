package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.config.ConfigProperties;
import com.semifinished.config.DataSourceProperties;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.util.Assert;
import com.semifinished.util.ParamsUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
        JsonNode db = params.remove("@ds");
        String dataSource = "";
        if (db != null) {
            dataSource = db.asText();
            dataSource = ParamsUtils.hasText(dataSource, configProperties.getDataSource());
        } else {
            dataSource = configProperties.getDataSource();
        }

        String finalDataSource = dataSource;
        Assert.isFalse(dataSourceProperties.getDataSource().containsKey(dataSource), () -> new ParamsException("数据源" + finalDataSource + "不存在"));
        sqlDefinition.setDataSource(dataSource);
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
