package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.SelectEnhance;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 处理不合法的别名
 * 通过替换查询结果的key，能处理各种奇怪的别名
 */
@Component
@Order(-1000)
public class IllLegalAliasEnhance implements SelectEnhance {

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        List<Column> illegalAlias = getIllegalAlias(sqlDefinition);
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

    /**
     * 获取所有的不合法别名
     *
     * @param sqlDefinition SQL定义信息
     * @return 不合法别名集合
     */
    private List<Column> getIllegalAlias(SqlDefinition sqlDefinition) {
        List<Column> illegalAlias = sqlDefinition.getIllegalAlias();
        if (CollectionUtils.isEmpty(illegalAlias)) {
            return Collections.emptyList();
        }
        List<Column> alias = new ArrayList<>(illegalAlias);

        List<SqlDefinition> joins = sqlDefinition.getJoin();
        if (CollectionUtils.isEmpty(joins)) {
            return alias;
        }

        for (SqlDefinition join : joins) {
            alias.addAll(getIllegalAlias(join));
        }
        return alias;
    }
}
