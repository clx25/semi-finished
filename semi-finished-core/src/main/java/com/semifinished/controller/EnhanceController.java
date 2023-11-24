package com.semifinished.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.annontation.RequestBodyValue;
import com.semifinished.annontation.RequestParamNode;
import com.semifinished.pojo.Result;
import com.semifinished.service.EnhanceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
public class EnhanceController {

    private final EnhanceService enhanceService;

    /**
     * 根据条件获取所有数据
     *
     * @param params 查询条件
     * @return 查询出的结果列表
     */
    @PostMapping(value = "enhance")
    public Object selectPostMapping(@RequestBody(required = false) ObjectNode params) {
        return Result.success(enhanceService.select(params));
    }

}
