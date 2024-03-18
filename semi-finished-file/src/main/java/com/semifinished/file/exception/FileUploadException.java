package com.semifinished.file.exception;

import com.semifinished.core.exception.ProjectRuntimeException;

/**
 * 对文件的操作异常
 */
public class FileUploadException extends ProjectRuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message,Exception e) {
        super(message,e);
    }
}
