package com.semifinished.auth.controller;

import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.service.AuthenticationService;
import com.semifinished.auth.service.UserService;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class SemiAuthenticationController {

    private final UserService userService;
    private final AuthProperties authProperties;
    private final AuthenticationService authenticationService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("current")
    public Result currentUser() {
        Assert.isFalse(authProperties.isAuthEnable(), () -> new AuthException("需要开启登录验证并登录"));
        return Result.success(userService.getCurrent());
    }


    /**
     * 获取验证码图片
     *
     * @return 包含key和验证码图片的base64字符
     * @throws IOException IOException
     */
    @GetMapping("captcha")
    public Result captcha() throws IOException {
        return authenticationService.createCaptchaImage();
    }


}
