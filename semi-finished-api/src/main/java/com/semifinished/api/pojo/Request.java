package com.semifinished.api.pojo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Request {
    /**
     * 组名
     */
    private String groupName;
    /**
     * 请求配置
     */
    private List<RequestConfig> requestConfigs;

    /**
     * 文件名
     */
    private String ___file_name;

    public List<RequestConfig> pathRequestConfigs() {
        if (requestConfigs == null) {
            requestConfigs = new ArrayList<>();
        }
        return requestConfigs;
    }

    @Data
    public static class RequestConfig {
        /**
         * 请求路径
         */
        private String path;
        /**
         * 请求信息
         */
        private ApiInfo apiInfo;

        public ApiInfo pathApiInfo() {
            if (apiInfo == null) {
                apiInfo = new ApiInfo();
            }
            return apiInfo;
        }
    }

    @Data
    public static class ApiInfo {
        /**
         * 版本
         */
        private int version;
        /**
         * 请求参数
         */
        private ObjectNode params;
        /**
         * 校验规则
         */
        private Map<String, ObjectNode> ruler;
    }
}
