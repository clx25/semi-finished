package com.semifinished.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties("semi-finished.auth")
public class AuthProperties {
    /**
     * token的有效期，单位秒
     */
    private long tokenDuration = 7 * 24 * 60 * 60;


    /**
     * 是否开启认证
     */
    private boolean authEnable = true;

    /**
     * 是否开启登录的验证码
     */
    private boolean loginCaptcha;

    /**
     * 是否开启注册的验证码
     */
    private boolean signupCaptcha;

    /**
     * token在header中的key
     */
    private String tokenKey = "token";

    /**
     * 管理员的角色编码
     */
    private String adminCode = "admin";

    /**
     * 跳过登录验证的路径
     */
    private Map<String, String> skip;
}
