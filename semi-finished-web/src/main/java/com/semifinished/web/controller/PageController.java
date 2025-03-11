package com.semifinished.web.controller;

import com.semifinished.api.listener.JsonApiInit;
import com.semifinished.core.pojo.Result;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PageController {

    @GetMapping("/groupName")
    public Result getGroupName(){
        return Result.success(JsonApiInit.apiRequestNameGroupMapping.values());
    }
}
