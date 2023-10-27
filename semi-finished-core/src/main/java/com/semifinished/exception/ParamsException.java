package com.semifinished.exception;


import com.semifinished.constant.ResultInfo;

/**
 * 请求参数异常
 */
public class ParamsException extends ProjectRuntimeException {
    public ParamsException(String message) {
        super(message);
    }

    public ParamsException(String message, Exception e) {
        super(message, e);
    }

    public ParamsException(ResultInfo resultInfo) {
        super(resultInfo);
    }
}
