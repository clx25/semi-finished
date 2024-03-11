package com.semifinished.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.utils.AuthUtils;
import com.semifinished.auth.utils.CaptchaCreator;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.util.SnowFlake;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.service.EnhanceService;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final SemiCache semiCache;
    private final SnowFlake snowFlake;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final EnhanceService enhanceService;

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
        semiCache.setValue(AuthCacheKey.CAPTCHA.getKey(), MapUtils.of(key, code));
        return Result.success(MapUtils.of("key", key, "img", image));
    }


    public Result updatePassword(ObjectNode params) {
        String id = params.get(configProperties.getIdKey()).asText(null);
        String password = params.get("password").asText(null);

        Assert.isFalse(StringUtils.hasText(id), () -> new ParamsException("用户id不能为空"));
        Assert.isFalse(StringUtils.hasText(password), () -> new ParamsException("用户密码不能为空"));
        sqlExecutorHolder.dataSource(configProperties.getDataSource()).update("update semi_user set password=:password where id=:id", MapUtils.of("password", JwtUtils.bCryptPasswordEncoder.encode(password), configProperties.getIdKey(), id));

        return Result.success();
    }

    /**
     * 获取当前登录用户数据
     *
     * @return 当前登录用户数据
     */
    public Object currentUser() {
        Map<String, ObjectNode> users = semiCache.getValue(AuthCacheKey.USER.getKey());
        Object userId = RequestUtils.getRequestAttributes(configProperties.getIdKey());
        if (users != null) {
            return users.get((String) userId);

        }

        String paramsStr = "{\"@tb\":\"semi_user\",\"@row\":1,\"id\":\"" + userId + "\",\"id:\":[{\"@tb\":\"semi_user_role\",\"@on\":\"user_id\",\"@\":\"role_id\"},{\"@tb\":\"semi_user_dept\",\"@on\":\"user_id\",\"@\":\"dept_id\"}]}";
        try {
            ObjectNode params = objectMapper.readValue(paramsStr, ObjectNode.class);
            Object user = enhanceService.query(params);
            AuthUtils.addUser(semiCache, (String) userId, (ObjectNode) user);
            return user;
        } catch (JsonProcessingException e) {
            throw new ConfigException("ObjectNode转换错误", e);
        }

    }


}
