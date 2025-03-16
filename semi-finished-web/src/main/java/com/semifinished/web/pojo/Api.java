package com.semifinished.web.pojo;

import lombok.Data;

@Data
public class Api {

    /**
     * 组名
     */
    private String groupName;

    /**
     * 简要描述
     */
    private String summary;

    /**
     * 请求路径
     */
    private String pattern;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 是否关闭接口
     */
    private boolean disabled;

    /**
     * 版本，高版本会覆盖低版本
     */
    private Integer version;

    /**
     * 额外自定义配置
     */
    private String config;


}
