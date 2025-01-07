package com.semifinished.auth.controller;

import com.semifinished.auth.service.AuthenticationService;
import com.semifinished.core.pojo.Result;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@AllArgsConstructor
public class SemiAuthenticationController {


    private final AuthenticationService authenticationService;


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
