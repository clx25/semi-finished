package com.semifinished.core.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.service.UpdateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class CommonController {

    private final UpdateService updateService;

    /**
     * 新增数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
    @PostMapping(value = "common", name = "SEMI-JSON-API")
    public Result add(@RequestBody ObjectNode params) {
        updateService.add(params);
        return Result.success();
    }

    /**
     * 修改数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
    @PutMapping(value = "common", name = "SEMI-JSON-API")
    public Result update(@RequestBody ObjectNode params) {
        updateService.update(params);
        return Result.success();
    }

    /**
     * 删除数据
     *
     * @param table 表名
     * @param id    主键数据
     * @return 执行结果
     */
    @DeleteMapping("common/{table}/{id}")
    public Result delete(@PathVariable String table, @PathVariable String id) {
        updateService.delete(table, id);
        return Result.success();
    }

    /**
     * 删除数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
//    @DeleteMapping("common")
//    public Result delete(@RequestParamNode ObjectNode params) {
//        updateService.delete(params);
//        return Result.success();
//    }

}
