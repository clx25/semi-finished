package com.semifinished.core.utils;

import com.semifinished.core.exception.CodeException;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

public class RequestUtils {
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    @Nullable
    public static <T> T getRequestAttributes(@NotNull String key) {
        Assert.isFalse(key == null, () -> new CodeException("attribute key不能为null"));
        return (T) getRequest().getAttribute(key);
    }

}
