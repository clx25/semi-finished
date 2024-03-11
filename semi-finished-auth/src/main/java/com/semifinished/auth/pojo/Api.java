package com.semifinished.auth.pojo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class Api {
    private String id;
    /**
     * 方法签名
     */
    private String signature;
    /**
     * 请求的名称
     */
    private String semiId;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求参数
     */
    private ObjectNode params;
    /**
     * 请求的url规则
     */
    private String pattern;
    /**
     * 类型，1内置，2跳过，3自定义
     */
    private int type;

    /**
     * 是否跳过认证
     */
    private boolean skipAuth;
}