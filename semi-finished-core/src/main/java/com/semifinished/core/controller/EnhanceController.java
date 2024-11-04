package com.semifinished.core.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.annontation.RequestParamNode;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.service.QueryService;
import com.semifinished.core.service.UpdateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
public class EnhanceController {

    private final QueryService queryService;
    private final UpdateService updateService;

    /**
     * 根据条件获取所有数据
     *
     * @param params 查询条件
     * @return 查询出的结果列表
     */
    @PostMapping(value = "enhance", name = "SEMI-JSON-API-POST-QUERY")
    public Object queryPostMapping(@RequestBody(required = false) ObjectNode params) {
        return Result.success(queryService.query(params));
    }

    /**
     * 配置接口GET请求的调用方法
     *
     * @param params 请求参数
     * @return 结果列表
     */
    @GetMapping(value = "enhance", name = "SEMI-JSON-API-GET")
    public Object queryGetMapping(@RequestParamNode(required = false) ObjectNode params) {
        return Result.success(queryService.query(params));
    }

    /**
     * 根据条件删除数据
     *
     * @param params 请求参数
     * @return 操作结果
     */
    @DeleteMapping(value = "enhance", name = "SEMI-JSON-API-DELETE")
    public Result delete(@RequestParamNode(required = false) ObjectNode params) {
        updateService.delete(params);
        return Result.success();
    }
}
