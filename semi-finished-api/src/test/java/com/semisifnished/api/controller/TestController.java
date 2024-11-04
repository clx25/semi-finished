package com.semisifnished.api.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.annotation.ApiGroup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @ApiGroup("tesa")
    @PostMapping(value = "/abc", name = "xxxx")
    public String test(ObjectNode objectNode) {
        return "bbb";
    }

}