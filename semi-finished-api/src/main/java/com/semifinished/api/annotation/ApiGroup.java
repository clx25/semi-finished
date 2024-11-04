package com.semifinished.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 添加在请求接口上，json文件的api参数会添加到接口的ObjectNode类型参数上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiGroup {
    /**
     * api组名
     */
    String value();
}
