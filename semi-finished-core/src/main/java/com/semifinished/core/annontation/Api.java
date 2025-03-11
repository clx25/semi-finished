package com.semifinished.core.annontation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Api {

    /**
     * 请求路径
     */
    String path();

    /**
     * 请求方式
     */
    String method() default "GET";
}
