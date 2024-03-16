package com.semifinished.core.config;


import com.semifinished.core.pojo.Desensitization;

import java.util.List;
import java.util.Map;

/**
 * 扩展配置
 */
public interface CoreConfigurer {


    /**
     * 添加脱敏规则，如果添加了自定义脱敏器，那么就不需要设置left，right
     */
    default void addDesensitize(List<Desensitization> desensitize) {
    }

    /**
     * 配置json文件中的组名与组对应的方法
     * 配置方式：
     * key：@RequestMapping的name属性
     * value：在json中的组名
     * <p>
     * 配置之后，组名的下一层会被当做api的path添加到name对应的方法上
     * 此时请求这个api，name对应的方法就会响应
     */
    default void addJsonConfig(Map<String, String> jsonConfig) {

    }

}
