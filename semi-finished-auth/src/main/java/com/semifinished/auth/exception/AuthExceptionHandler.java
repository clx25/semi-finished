package com.semifinished.auth.exception;

import com.semifinished.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.NestedServletException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE-3000)
public class AuthExceptionHandler {

    /**
     * 认证异常
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result authException(AuthException e) {
        return Result.info(e.getCode(), e.getMessage());
    }
}
