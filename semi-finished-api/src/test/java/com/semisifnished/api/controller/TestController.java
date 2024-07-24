package com.semisifnished.api.controller;

import com.semifinished.api.annotation.JsonApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @JsonApi(group = "tesa")
    @PostMapping(value = "/abc", name = "xxxx")
    public String test() {
        return "bbb";
    }

}