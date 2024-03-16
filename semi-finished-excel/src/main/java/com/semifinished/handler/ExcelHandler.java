package com.semifinished.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface ExcelHandler {

    void handler(String table, List<ObjectNode> excelData);


    String code();
}
