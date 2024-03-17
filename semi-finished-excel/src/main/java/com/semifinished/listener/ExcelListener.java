package com.semifinished.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 解析excel
 */
@AllArgsConstructor
public class ExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private final ArrayNode rows;
    private final Map<String, String> headerConfig;
    private final Map<Integer, String> header;


    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        parseHead(headMap);
    }

    private void parseHead(Map<Integer, String> excel) {
        for (Map.Entry<Integer, String> entry : excel.entrySet()) {
            for (Map.Entry<String, String> column : headerConfig.entrySet()) {
                if (StringUtils.hasText(entry.getValue()) && Objects.equals(column.getValue(), entry.getValue())) {
                    header.put(entry.getKey(), column.getKey());
                }
            }
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        if (header.isEmpty()) {
            parseHead(data);
            return;
        }
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<Integer, String> entry : header.entrySet()) {
            objectNode.put(entry.getValue(), data.get(entry.getKey()));
        }
        rows.add(objectNode);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }


}
