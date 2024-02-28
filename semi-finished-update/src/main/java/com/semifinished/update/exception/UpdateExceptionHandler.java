package com.semifinished.update.exception;

import com.semifinished.core.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class UpdateExceptionHandler {

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
        return Result.error(HttpStatus.BAD_REQUEST.value(), msg(e));
    }

    private String msg(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            String errorCode = String.valueOf(((SQLException) cause).getErrorCode());
            String message = cause.getMessage();
            message = message.substring(message.indexOf("column '")+8);
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
