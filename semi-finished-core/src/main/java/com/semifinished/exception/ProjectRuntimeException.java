package com.semifinished.exception;


import com.semifinished.constant.ResultInfo;

/**
 * 项目中的所有运行时异常
 */
public class ProjectRuntimeException extends RuntimeException {
    private int code;


    public ProjectRuntimeException(ResultInfo resultInfo) {
        super(resultInfo.getMsg());
        this.code = resultInfo.getCode();
    }


    public ProjectRuntimeException(String message) {
        super(message);
    }

    public ProjectRuntimeException(String message, Exception e) {
        super(message, e);
    }

    public int getCode() {
        return code;
    }
}
