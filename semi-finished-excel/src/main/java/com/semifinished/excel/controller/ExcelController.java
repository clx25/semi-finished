package com.semifinished.excel.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.pojo.Result;
import com.semifinished.excel.service.ExcelService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    /**
     * excel上传
     *
     * @param file 文件
     */
    @PostMapping(value = "excel", name = "SEMI-JSON-API-EXCEL")
    public Result excel(MultipartFile file, @ModelAttribute ObjectNode params) {
        excelService.parseExcel(file,params);
        return Result.success();
    }
}
