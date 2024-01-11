package com.semifinished.config;


import com.semifinished.pojo.Desensitization;

import java.util.List;

/**
 * 扩展配置
 */
public interface CoreConfigurer {


    /**
     * 添加脱敏规则，如果添加了自定义脱敏器，那么就不需要设置left，right
     */
    default void addDesensitize(List<Desensitization> desensitize) {
    }


}
