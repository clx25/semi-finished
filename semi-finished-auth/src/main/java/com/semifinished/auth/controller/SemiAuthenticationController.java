package com.semifinished.auth.controller;

import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.service.AuthenticationService;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class SemiAuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthProperties authProperties;


    /**
     * 获取当前登录用户信息
     */
    @PostMapping("currentUser")
    public Result currentUser() {
        Assert.isFalse(authProperties.isAuthEnable(), () -> new AuthException("需要开启登录验证并登录"));
        Object user = authenticationService.currentUser();
        return Result.success(user);
    }


    /**
     * 修改密码
     * @param params
     * @return
     */
//    @PutMapping("password")
//    public Result updatePassword(@RequestBody ObjectNode params) {
//        return authenticationService.updatePassword(params);
//    }

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
