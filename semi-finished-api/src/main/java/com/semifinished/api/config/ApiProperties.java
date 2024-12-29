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
     * 开启无法使用后端配置的json,前端需要传递完整的查询参数
     * 关闭则只能使用后端配置的json，无法使用通用查询，前端传递完整参数无效
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
