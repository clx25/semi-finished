package com.semifinished.annontation;

import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * spring boot未提供post请求，json格式参数的非pojo接收方式
 * 如有这样一个controller,使用post,json格式传参，该方法无法接收数据
 * <pre>
 *     public void login(String username,String password){
 *
 *     }
 * </pre>
 * 这样就可以接收数据了
 * <pre>
 *     public void login(@RequestValue String username, @RequestValue String password){
 *
 *     }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBodyValue {

    /**
     * 参数名称
     */
    String name() default "";

    /**
     * 是否必须
     */
    boolean required() default true;

    /**
     * 默认值
     */
    String defaultValue() default ValueConstants.DEFAULT_NONE;
}
