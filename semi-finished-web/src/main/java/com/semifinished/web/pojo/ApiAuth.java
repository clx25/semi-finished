package com.semifinished.web.pojo;

import lombok.Data;

/**
 * api权限
 */
@Data
public class ApiAuth {


    private Integer id;

    /**
     * 匹配规则
     */
    private String pattern;

    /**
     * 请求方式
     */
    private String method;

    /**
     * 是否关闭权限
     */
    private boolean disabled;
}
