package com.semifinished.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
    @PostMapping(value = "common", name = "SEMI-JSON-API-POST")
    public Result add(@RequestBody JsonNode params) {
        String id = updateService.add(params);
        return Result.success(id);
    }

    /**
     * 修改数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
    @PutMapping(value = "common", name = "SEMI-JSON-API-PUT")
    public Result update(@RequestBody JsonNode params) {
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
    @DeleteMapping(value = "common/{table}/{id}", name = "SEMI-COMMON-DELETE")
    public Result delete(@PathVariable String table, @PathVariable String id) {
//        updateService.delete(table, id);
        return Result.success();
    }


//    /**
//     * 批量新增数据
//     *
//     * @param params 请求参数
//     * @return 操作结果
//     */
//    @PostMapping(value = "common/batch", name = "SEMI-JSON-API-POST-BATCH")
//    public Result batchAdd(@RequestBody JsonNode params) {
//        updateService.batchAdd(params);
//        return Result.success();
//    }

//    /**
//     * 批量新增数据
//     *
//     * @param params 请求参数
//     * @return 操作结果
//     */
//    @PutMapping(value = "common/batch", name = "SEMI-JSON-API-PUT-BATCH")
//    public Result batchUpdate(@RequestBody JsonNode params) {
//        updateService.batchUpdate(params);
//        return Result.success();
//    }


}
