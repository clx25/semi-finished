package com.semifinished.core.service.enhance.update;

import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 当新增/修改数据时，如果有字段为null，校验数据库字段是否支持为null
 */
//@Component
@AllArgsConstructor
public class NullAbleValidateEnhance implements AfterUpdateEnhance {
    private final SemiCache semiCache;
    private final ConfigProperties configProperties;

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        String table = sqlDefinition.getTable();
        List<Column> columnList = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + sqlDefinition.getDataSource());
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();

        String nullValue = columnList.stream()
                .filter(c -> !c.isNullAble())
                .filter(c -> table.equals(c.getTable()))
                .filter(c -> !configProperties.getIdKey().equals(c.getColumn()))
                .filter(c -> valueConditions.stream()
                        .noneMatch(v -> c.getColumn().equals(v.getColumn()) && v.getValue() != null)
                ).map(c -> ParamsUtils.hasText(c.getAlias(), c.getColumn()))
                .collect(Collectors.joining(","));

        Assert.isBlank(nullValue, () -> new ParamsException("%s参数不能为空", nullValue));

    }
}
