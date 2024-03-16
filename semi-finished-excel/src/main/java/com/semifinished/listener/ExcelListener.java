package com.semifinished.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 解析excel
 */
@AllArgsConstructor
public class ExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private final List<ObjectNode> excelData;
    private final Map<String, String> excelColumns;
    private final Map<Integer, String> head = new HashMap<>();


    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        parseHead(headMap);
    }

    private void parseHead(Map<Integer, String> excel) {
        for (Map.Entry<Integer, String> entry : excel.entrySet()) {
            for (Map.Entry<String, String> column : excelColumns.entrySet()) {
                if (StringUtils.hasText(entry.getValue()) && Objects.equals(column.getValue(), entry.getValue())) {
                    head.put(entry.getKey(), column.getKey());
                }
            }
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        if (head.isEmpty()) {
            parseHead(data);
            return;
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<Integer, String> entry : head.entrySet()) {
            objectNode.put(entry.getValue(), data.get(entry.getKey()));
        }
        excelData.add(objectNode);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


}
