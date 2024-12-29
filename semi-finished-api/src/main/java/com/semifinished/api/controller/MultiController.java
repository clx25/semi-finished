package com.semifinished.api.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.service.MultiService;
import com.semifinished.core.pojo.Result;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class MultiController {

    private final MultiService multiService;

    @PostMapping(value = "multi", name = "SEMI_COMMON_MULTI")
    public Result multi(@RequestBody ObjectNode params) {
        multiService.multi(params);
        return Result.success();
    }
}
