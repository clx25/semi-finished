package com.semifinished.auth.config;

import lombok.Getter;

@Getter
public enum AuthResultInfo {

    UNAUTHORIZED("未认证", 401),
    AUTHENTICATION("账号或密码错误", 4002),
    CAPTCHA_ERROR("验证码错误", 4003);

    private final String msg;
    private final int code;

    AuthResultInfo(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }
}
