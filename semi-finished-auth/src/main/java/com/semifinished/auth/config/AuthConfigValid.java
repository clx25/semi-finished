package com.semifinished.auth.config;

import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AuthConfigValid implements InitializingBean {

    private final AuthProperties authProperties;

    @Override
    public void afterPropertiesSet() {
        log.info((authProperties.isLoginCaptcha() ? "启用" : "关闭") + "登录验证码");

        log.info((authProperties.isSignupCaptcha() ? "启用" : "关闭") + "注册验证码");

        log.info(authProperties.isAuthEnable() ? "登录验证开启" : "登录验证关闭");

        Assert.hasNotText(authProperties.getTokenKey(), () -> new ConfigException("未配置tokenKey"));
        Assert.hasNotText(authProperties.getAdminCode(), () -> new ConfigException("未配置管理员编码"));
    }
}
