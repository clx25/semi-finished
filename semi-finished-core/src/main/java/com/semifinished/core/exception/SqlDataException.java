package com.semifinished.core.exception;

/**
 * 由sql构建器构造出的sql异常，如sql为null,没有基础sql
 * todo 去掉这个异常，使用codeException
 */
public class SqlDataException extends ProjectRuntimeException {
    public SqlDataException(String message) {
        super(message);
    }
}
