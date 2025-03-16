package com.semifinished.excel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.service.UpdateAbstractService;
import com.semifinished.excel.handler.ExcelHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class CommonHandler implements ExcelHandler {

    private final UpdateAbstractService updateAbstractService;

    @Override
    public void handle(ObjectNode configs, ArrayNode rows, Map<Integer, String> header) {
//        updateService.batchAdd(rows);
    }

    @Override
    public String[] path() {
        return new String[]{"validte"};
    }
}
