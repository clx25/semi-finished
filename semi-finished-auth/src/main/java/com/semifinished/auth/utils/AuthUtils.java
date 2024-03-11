package com.semifinished.auth.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.RequestUtils;

import java.util.HashMap;
import java.util.Map;

public class AuthUtils {

    public static ObjectNode getCurrent(SemiCache semiCache, String idKey) {
        String id = RequestUtils.getRequestAttributes(idKey);
        Map<String, ObjectNode> userMap = semiCache.getValue(AuthCacheKey.USER.getKey(), HashMap::new);
        return userMap.get(id);
    }

    public static void addUser(SemiCache semiCache, String id, ObjectNode user) {
        Map<String, ObjectNode> userCache = semiCache.getValue(AuthCacheKey.USER.getKey(), HashMap::new);
        userCache.put(id, user);
    }


    /**
     * 匹配验证码
     *
     * @param key     验证码的key
     * @param captcha 验证码
     * @return true, 验证通过或不需要验证，false验证失败
     */
    public static boolean captchaMatch(SemiCache semiCache, String key, String captcha) {
        if (captcha == null) {
            return false;
        }
        Map<String, String> captchaMap = semiCache.getValue(AuthCacheKey.CAPTCHA.getKey());
        if (captchaMap == null) {
            return false;
        }
        if (captcha.equalsIgnoreCase(captchaMap.get(key))) {
            captchaMap.remove(key);
            return true;
        }

        return false;
    }

    public static Result info(AuthResultInfo info) {
        return Result.info(info.getCode(), info.getMsg());
    }
}
