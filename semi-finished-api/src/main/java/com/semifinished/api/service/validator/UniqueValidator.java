package com.semifinished.api.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UniqueValidator implements Validator {
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public boolean validate(String field, JsonNode value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"unique".equalsIgnoreCase(pattern)) {
            return false;
        }
        if (value == null || value.isMissingNode() || value.isEmpty()) {
            return true;
        }
        String sql = "select 1  from %s where %s=:%s";

        String table = sqlDefinition.getTable();

        for (ValueCondition valueCondition : sqlDefinition.getValueCondition()) {
            if (table.equals(valueCondition.getTable()) && field.equals(valueCondition.getColumn())) {
                sql = String.format(sql, table, field, field);
                boolean match = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                        .existMatch(sql, MapUtils.of(field, value.asText()));

                Assert.isTrue(match, () -> new ParamsException(msg));
            }
        }

        return true;
    }


}
