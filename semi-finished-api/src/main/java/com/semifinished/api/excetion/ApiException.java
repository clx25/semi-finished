package com.semifinished.api.excetion;

import com.semifinished.core.constant.ResultInfo;
import com.semifinished.core.exception.ProjectRuntimeException;

public class ApiException extends ProjectRuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Exception e) {
        super(message, e);
    }
}
