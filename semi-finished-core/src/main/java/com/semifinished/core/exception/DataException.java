package com.semifinished.core.exception;


import com.semifinished.core.constant.ResultInfo;

/**
 * 数据完整性异常，如用户必须有的部门，某个用户没有
 */
public class DataException extends ProjectRuntimeException {
    public DataException(ResultInfo resultInfo) {
        super(resultInfo);
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Exception e) {
        super(message, e);
    }
}
