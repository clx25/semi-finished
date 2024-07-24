package com.semifinished.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("semi-finished.api")
public class ApiProperties {
    /**
     * 通用查询开关，true表示启用，false表示关闭
     */
    private boolean commonApiEnable = false;

    /**
     * 外部的api文件夹与jar包所在目录的的相对路径
     */
    private String apiFolder = "SEMI-API";

    /**
     * 跨域配置
     */
    private boolean crossOrigin = false;

    /**
     * 是否启用swagger页面
     */
    private boolean SwaggerEnable = true;
}
