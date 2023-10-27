package com.semifinished.exception;

/**
 * 由代码写得不严谨产生的异常，如一些工具类的入参异常
 */
public class CodeException extends ProjectRuntimeException {
    public CodeException(String message) {
        super(message);
    }
}
