package com.semifinished.exception;


import com.semifinished.constant.ResultInfo;
import com.semifinished.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;

/**
 * 异常的统一处理类
 * //todo 开发与上线使用不同的错误提示？
 */
@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exception(Exception e) {
        log.error("未知错误", e);
        return Result.error(500, "未知错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result projectRuntimeException(ProjectRuntimeException e) {
        log.error("未知错误", e);
        return Result.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result badSqlGrammarException(BadSqlGrammarException e) {
        log.error("sql执行错误", e);
        return Result.info(ResultInfo.SQL_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result constraintViolationException(ConstraintViolationException e) {
        log.info("参数校验异常", e);
        return Result.error(400, splitMessage(e.getConstraintViolations()));
    }

    public String splitMessage(Set<ConstraintViolation<?>> set) {
        if (set.size() == 0) {
            return "";
        }
        List<String> msgList = new ArrayList<>();
        for (ConstraintViolation<?> violation : set) {
            msgList.add(violation.getMessage());
        }
        return String.join(",", msgList);
    }

    /**
     * 一些必要的配置异常
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result configException(ConfigException e) {
        log.error(e.getMessage());
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "网络异常");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("请求参数错误", e);
        List<FieldError> fieldErrors = e.getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            if (message.length() != 0) {
                message.append(",");
            }
            message.append(fieldError.getDefaultMessage()).append(" ");
        }
        return Result.error(4005, message.toString());

    }

    /**
     * 使用sql构造器时出现异常
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result sqlDataException(SqlDataException e) {
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    /**
     * 请求参数错误
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result paramsException(ParamsException e) {
        log.info(ResultInfo.PAYLOAD_ERROR.getMsg(), e);
        return Result.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 异步编排里面的方法错误
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result completionException(CompletionException e) {
        log.error("异步编排错误", e);
        Throwable throwable = e.getCause();
        String msg = "未知错误";
        if (throwable instanceof ParamsException) {
            msg = throwable.getMessage();
        }
        return Result.error(HttpStatus.BAD_REQUEST.value(), msg);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result httpMessageConversionException(HttpMessageConversionException e) {
        log.info("请求参数错误", e);
        return Result.info(ResultInfo.PAYLOAD_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.info(ResultInfo.METHOD_ERROR.getMsg(), e);
        return Result.info(ResultInfo.METHOD_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.info(ResultInfo.PAYLOAD_ERROR.getMsg(), e);
        return Result.error(ResultInfo.PAYLOAD_ERROR.getCode(), e.getParameterName() + "参数不能为空");
    }
}
