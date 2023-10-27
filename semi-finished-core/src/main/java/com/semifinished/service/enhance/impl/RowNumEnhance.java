package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.SelectEnhance;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据请求参数截取返回的行
 */
@Order(-1000)
@Component
public class RowNumEnhance implements SelectEnhance {

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        int rowStart = sqlDefinition.getRowStart();
        if (rowStart < 1) {
            return;
        }
        int rowEnd = sqlDefinition.getRowEnd();

        List<ObjectNode> rows = new ArrayList<>();
        for (int i = 1; i < records.size() + 1; i++) {
            if ((rowEnd == 0 || rowEnd == rowStart) && i == rowStart) {
                rows.add(records.get(i - 1));
                break;
            }
            if (i >= rowStart && i <= rowEnd) {
                rows.add(records.get(i - 1));
            }
        }
        page.setRecords(rows);
    }
}
