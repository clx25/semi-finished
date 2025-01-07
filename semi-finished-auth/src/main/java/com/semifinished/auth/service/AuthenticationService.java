package com.semifinished.auth.service;

import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.utils.CaptchaCreator;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.jdbc.util.SnowFlake;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.MapUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final SemiCache semiCache;
    private final SnowFlake snowFlake;

    /**
     * 创建验证码图片
     *
     * @return 包含base64编码后的验证码图片
     * @throws IOException 图片生成错误
     */
    public Result createCaptchaImage() throws IOException {
        String code = CaptchaCreator.create(4);
        String image = CaptchaCreator.createImage(code);
        String key = String.valueOf(snowFlake.getId());
        semiCache.addHashValue(AuthCacheKey.CAPTCHA.getKey(), key, code);
        return Result.success(MapUtils.of("key", key, "img", image));
    }


}
