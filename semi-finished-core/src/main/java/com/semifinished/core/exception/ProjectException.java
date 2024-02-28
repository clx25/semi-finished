package com.semifinished.core.exception;

/**
 * 项目中的检查异常
 */
public class ProjectException extends Exception {
    public ProjectException(String message) {
        super(message);
    }

    public ProjectException(String message, Exception e) {
        super(message, e);
    }
}
