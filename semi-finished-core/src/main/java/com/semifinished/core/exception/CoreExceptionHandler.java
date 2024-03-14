package com.semifinished.core.exception;


import com.fasterxml.jackson.core.JsonParseException;
import com.semifinished.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
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
public class CoreExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result exception(Exception e) {
        log.error("未知错误", e);
        return Result.info(500, "未知错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result projectRuntimeException(ProjectRuntimeException e) {
        log.error("未知错误", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result JsonParseException(JsonParseException e) {
        log.debug("json序列化错误", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), "请求参数错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result badSqlGrammarException(BadSqlGrammarException e) {
        log.error("sql执行错误", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), "请求参数错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result constraintViolationException(ConstraintViolationException e) {
        log.debug("参数校验异常", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), splitMessage(e.getConstraintViolations()));
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
        return Result.info(HttpStatus.INTERNAL_SERVER_ERROR.value(), "网络异常");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.debug("请求参数错误", e);
        List<FieldError> fieldErrors = e.getFieldErrors();
        StringBuilder message = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            if (message.length() != 0) {
                message.append(",");
            }
            message.append(fieldError.getDefaultMessage()).append(" ");
        }
        return Result.info(HttpStatus.BAD_REQUEST.value(), message.toString());

    }


    /**
     * 请求参数错误
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result paramsException(ParamsException e) {
        log.debug("参数错误", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), e.getMessage());
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
        return Result.info(HttpStatus.BAD_REQUEST.value(), msg);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result httpMessageConversionException(HttpMessageConversionException e) {
        log.debug("参数错误", e);
        return Result.info(HttpStatus.BAD_REQUEST.value(), "请求参数错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.debug("请求方式错误", e);
        return Result.info(HttpStatus.METHOD_NOT_ALLOWED.value(), "请求方式错误");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.info(HttpStatus.BAD_REQUEST.value(), e.getParameterName() + "参数不能为空");
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result dataIntegrityViolationException(DataIntegrityViolationException e) {
        return result(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result uncategorizedSQLException(UncategorizedSQLException e) {
        return result(e);
    }

    private Result result(Exception e) {
        return Result.info(HttpStatus.BAD_REQUEST.value(), msg(e));
    }

    private String msg(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            String errorCode = String.valueOf(((SQLException) cause).getErrorCode());
            String message = cause.getMessage();
            message = message.substring(message.indexOf("column '") + 8);
            message = message.substring(0, message.indexOf("'"));
            switch (errorCode) {
                case "1292":
                    return "字段" + message + "日期或时间格式错误";
                case "1366":
                    return "字段" + message + "数据类型不匹配";
                case "1048":
                    return "字段" + message + "不能为null";
            }
        }
        return "参数错误";
    }
}
