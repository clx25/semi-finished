package com.semifinished.file.exception;

import com.semifinished.core.constant.ResultInfo;
import com.semifinished.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result missingServletRequestParameterException(FileUploadException e) {
        log.info(ResultInfo.UPLOAD_ERROR.getMsg(), e);
        return Result.info(ResultInfo.UPLOAD_ERROR.getCode(), ResultInfo.UPLOAD_ERROR.getMsg());
    }
}
