package com.semifinished.core.service.enhance.validator;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@AllArgsConstructor
public class UniqueValidator implements Validator {
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public boolean validate(String field, String value, String pattern, String msg, SqlDefinition sqlDefinition) {
        if (!"unique".equalsIgnoreCase(pattern)) {
            return false;
        }
        String sql = "select 1  from %s where %s=:%s";

        String table = sqlDefinition.getTable();

        for (ValueCondition valueCondition : sqlDefinition.getValueCondition()) {
            if (table.equals(valueCondition.getTable()) && field.equals(valueCondition.getColumn())) {
                sql = String.format(sql, table, field, field);
                boolean match = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                        .existMatch(sql, MapUtils.of(field, value));

                Assert.isTrue(match,()->new ParamsException(msg));
            }
        }

        return true;
    }


}
