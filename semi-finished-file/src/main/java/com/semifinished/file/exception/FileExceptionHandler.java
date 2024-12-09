package com.semifinished.file.exception;

import com.semifinished.core.constant.ResultInfo;
import com.semifinished.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE-2000)
@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result missingServletRequestParameterException(FileUploadException e) {
        log.info(ResultInfo.UPLOAD_ERROR.getMsg(), e);
        return Result.info(ResultInfo.UPLOAD_ERROR.getCode(), ResultInfo.UPLOAD_ERROR.getMsg());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result fileNotFoundException(FileNotFoundException e) {
        log.info(e.getMessage(),e);
        return Result.info(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }
}
