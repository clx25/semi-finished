package com.semifinished.auth.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 验证码校验
 */

@Order(-190)
@Component
@AllArgsConstructor
public class CaptchaEnhance implements AfterUpdateEnhance, AfterQueryEnhance {

    private final AuthProperties authProperties;
    private final SemiCache semiCache;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        JsonNode params = sqlDefinition.getRawParams();

        if (!authProperties.isCaptcha()) {
            return;
        }
        Assert.isFalse(params.has("key"), () -> new ParamsException("缺少验证码key"));
        Assert.isFalse(params.has("captcha"), () -> new ParamsException("缺少验证码"));

        boolean match = captchaMatch(semiCache, params.path("key").asText(), params.path("captcha").asText());

        Assert.isFalse(match, () -> new AuthException(AuthResultInfo.CAPTCHA_ERROR));
    }

    /**
     * 匹配验证码
     *
     * @param key     验证码的key
     * @param captcha 验证码
     * @return true, 验证通过或不需要验证，false验证失败
     */
    private boolean captchaMatch(SemiCache semiCache, String key, String captcha) {
        if (captcha == null) {
            return false;
        }
        String matchCaptcha = semiCache.getValue(AuthCacheKey.CAPTCHA.getKey(), key);
        if (matchCaptcha == null) {
            return false;
        }
        if (captcha.equalsIgnoreCase(matchCaptcha)) {
            semiCache.removeValue(AuthCacheKey.CAPTCHA.getKey(), key);
            return true;
        }

        return false;
    }
}
