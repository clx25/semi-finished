package com.semifinished.core.annontation;

import org.springframework.web.bind.annotation.ValueConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将get请求参数转为ObjectNode
 * 如有这样一个controller,使用get,url传参，该方法无法接收数据
 * <pre>
 *     public void login(ObjectNode node){
 *
 *     }
 * </pre>
 * 这样就可以接收数据了
 * <pre>
 *     public void login(@RequestNode ObjectNode node){
 *
 *     }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParamNode {

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
