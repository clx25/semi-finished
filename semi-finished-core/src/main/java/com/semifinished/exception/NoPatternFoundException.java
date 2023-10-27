package com.semifinished.exception;

/**
 * 没有找到与请求匹配的url规则
 */
public class NoPatternFoundException extends ProjectRuntimeException {
    public NoPatternFoundException(String message) {
        super(message);
    }
}
