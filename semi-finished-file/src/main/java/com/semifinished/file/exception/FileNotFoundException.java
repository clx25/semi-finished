package com.semifinished.file.exception;

import com.semifinished.core.exception.ProjectRuntimeException;

/**
 * 文件找不到
 */
public class FileNotFoundException extends ProjectRuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
