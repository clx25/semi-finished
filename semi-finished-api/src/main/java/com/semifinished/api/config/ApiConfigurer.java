package com.semifinished.api.config;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public interface ApiConfigurer {

    /**
     * 配置json文件中的组名与组对应的方法
     * 配置方式：
     * key：@RequestMapping的name属性
     * value：在json中的组名
     * <p>
     * 配置之后，组名的下一层会被当做api的path添加到name对应的方法上
     * 此时请求这个api，name对应的方法就会响应
     */
    default void apiRequestNameGroupMapping(Map<String, String> apiRequestNameGroupMapping) {

    }

    /**
     * 调整api
     *
     * @param apiMap 包含api的信息
     */
    default void addApiMap(Map<String, Map<String, ObjectNode>> apiMap) {

    }
}
