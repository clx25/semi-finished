package com.semifinished.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.utils.CaptchaCreator;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.jdbc.util.SnowFlake;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.service.QueryService;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final SemiCache semiCache;
    private final SnowFlake snowFlake;
    private final QueryService queryService;
    private final ConfigProperties configProperties;

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
        semiCache.addValue(AuthCacheKey.CAPTCHA.getKey(), key, code);
        return Result.success(MapUtils.of("key", key, "img", image));
    }


    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public ObjectNode getCurrent() {
        String idKey = configProperties.getIdKey();
        String userId = RequestUtils.getRequestAttributes(idKey);
        ObjectNode user = semiCache.getValue(AuthCacheKey.USER.getKey(), userId);

        if (user != null) {
            return user;
        }
        Map<String, JsonNode> apiMap = semiCache.getValue(CoreCacheKey.CUSTOM_API.getKey(), "POST");

        ObjectNode params = apiMap.getOrDefault("/login", MissingNode.getInstance()).with("params");
        params.remove("@bean");
        params.remove("username$$");
        params.put(idKey, userId);

        user = (ObjectNode) queryService.query(params);
        semiCache.addValue(AuthCacheKey.USER.getKey(), userId, user);

        return user;

    }
}
