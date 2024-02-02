package com.semifinished.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.config.ConfigProperties;
import com.semifinished.pojo.Result;
import com.semifinished.service.UpdateService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class CommonController {

    private final UpdateService updateService;
    private final ConfigProperties configProperties;

    @PostMapping("common")
    public Result add(@RequestBody ObjectNode params) {

        updateService.add(params);
        return Result.success();
    }

    @PutMapping("common")
    public Result update(@RequestBody ObjectNode params) {
        updateService.update(params);
        return Result.success();
    }

    @DeleteMapping("common/{id}")
    public Result delete(@PathVariable String id) {
        updateService.delete(id);
        return Result.success();
    }

    @PostMapping("has")
    public Result has(@RequestBody ObjectNode params) {
        return Result.success();
    }
}
