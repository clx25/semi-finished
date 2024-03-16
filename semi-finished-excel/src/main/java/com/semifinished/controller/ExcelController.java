package com.semifinished.controller;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.cache.ExcelCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.utils.Assert;
import com.semifinished.listener.ExcelListener;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@AllArgsConstructor
public class ExcelController {

    private final SemiCache semiCache;
    private final ObjectMapper objectMapper;

    /**
     * excel上传
     *
     * @param code 唯一编码
     * @param file 文件
     * @throws IOException 文件操作异常
     */
    @PostMapping("excel/{code}")
    public void excel(@PathVariable String code, MultipartFile file) throws IOException {

    }
}
