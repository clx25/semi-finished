package com.semifinished.core.exception;


/**
 * 请求参数异常
 */
public class ParamsException extends ProjectRuntimeException {

    public ParamsException(String format) {
        super(format);
    }

    public ParamsException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ParamsException(String message, Exception e) {
        super(message, e);
    }

}
