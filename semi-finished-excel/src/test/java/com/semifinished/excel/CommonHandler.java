package com.semifinished.excel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.service.UpdateService;
import com.semifinished.handler.ExcelHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class CommonHandler implements ExcelHandler {

    private final UpdateService updateService;

    @Override
    public void handle(ObjectNode configs, ArrayNode rows, Map<Integer, String> header) {
        updateService.batchAdd(rows);
    }

    @Override
    public String[] path() {
        return new String[]{"validte"};
    }
}
