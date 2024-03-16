package com.semifinished.service;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.cache.ExcelCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import com.semifinished.handler.ExcelHandler;
import com.semifinished.listener.ExcelListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class ExcelService {
    private final SemiCache semiCache;
    private final ObjectMapper objectMapper;
    private final Map<String, ExcelHandler> excelHandlerMap = new HashMap<>();

    public ExcelService(SemiCache semiCache, ObjectMapper objectMapper, List<ExcelHandler> excelHandlers) {
        this.semiCache = semiCache;
        this.objectMapper = objectMapper;

        if (excelHandlers.isEmpty()) {
            return;
        }
        for (ExcelHandler excelHandler : excelHandlers) {
            excelHandlerMap.put(excelHandler.code(), excelHandler);
        }

    }

    public void parseExcel(String code, MultipartFile file) {
        Map<String, ObjectNode> excel = semiCache.getValue(ExcelCacheKey.EXCEL.getKey(), code);

        ObjectNode configs = excel.get(code);
        Assert.isTrue(configs == null, () -> new ParamsException("未配置该excel解析code"));

        String table = configs.get("table").asText();

        JsonNode headerNode = configs.path("header");
        Map<String, String> header = objectMapper.convertValue(headerNode, new TypeReference<Map<String, String>>() {
        });
        List<ObjectNode> excelData = new ArrayList<>();

        ExcelHandler excelHandler = excelHandlerMap.get(code);
        Assert.isTrue(excelHandler == null, () -> new ParamsException("未配置该excel处理类"));
        try {
            EasyExcel.read(file.getInputStream(), new ExcelListener(excelData, header)).sheet().doRead();
            excelHandler.handler(table,excelData);
        } catch (IOException e) {
            throw new ParamsException("excel文件异常");
        }


    }


}
