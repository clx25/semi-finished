package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.Page;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 处理不合法的别名
 * 通过替换查询结果的key，能处理各种奇怪的别名
 */
@Component
@Order(-1000)
public class IllLegalAliasEnhance implements AfterQueryEnhance {

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        List<Column> illegalAlias = QuerySqlCombiner.illegalAlias(sqlDefinition);
        if (illegalAlias.isEmpty()) {
            return;
        }
        replaceAlias(records, illegalAlias);
    }

    /**
     * 替换别名
     *
     * @param records      查询结果集合
     * @param illegalAlias 不合法别名集合
     */
    private void replaceAlias(List<ObjectNode> records, List<Column> illegalAlias) {
        for (ObjectNode record : records) {
            for (Column alias : illegalAlias) {
                String column = alias.getColumn();
                if (record.has(column)) {
                    record.set(alias.getAlias(), record.remove(column));
                }
            }
        }
    }

}
