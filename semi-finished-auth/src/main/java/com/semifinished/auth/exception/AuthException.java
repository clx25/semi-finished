package com.semifinished.auth.exception;

import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.core.exception.ProjectRuntimeException;

/**
 * 认证异常
 */

public class AuthException extends ProjectRuntimeException {

    public AuthException(AuthResultInfo info) {
        super(info.getCode(), info.getMsg());
    }

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Exception e) {
        super(message, e);
    }
}
