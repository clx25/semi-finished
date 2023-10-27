package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlDefinition;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 指定数据源
 * <pre>
 *     "@db":"database"
 * </pre>
 */
@Order(-500)
@Component
@AllArgsConstructor
public class DataSourceParser implements ParamsParser {

    @Override
    public void parser(ObjectNode params, SqlDefinition sqlDefinition) {
        JsonNode db = params.remove("@db");
        sqlDefinition.setDataSource(db == null ? null : db.asText());
    }
}
