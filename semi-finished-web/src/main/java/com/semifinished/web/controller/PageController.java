package com.semifinished.web.controller;

import com.semifinished.api.listener.JsonApiInit;
import com.semifinished.core.listener.RefreshCacheApplication;
import com.semifinished.core.pojo.Result;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class PageController   {
    private final RefreshCacheApplication refreshCacheApplication;

    @GetMapping("/groupName")
    public Result getGroupName() {
        return Result.success(JsonApiInit.apiRequestNameGroupMapping.values());
    }
    // @RequestMapping("/error")
    // public Result handleError(HttpServletRequest request) {
    //     Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    //     Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
    //     return Result.info(statusCode,exception != null ? exception.getMessage() : "未知错误");
    // }
}
