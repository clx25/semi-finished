package com.semifinished.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.CaptchaCreator;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.annontation.Api;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.util.SnowFlake;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.QueryAbstractService;
import com.semifinished.core.service.QueryService;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.core.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(path = "/login", method = "post")
@Service
@RequiredArgsConstructor
public class AuthenticationService extends QueryAbstractService implements QueryService {

    private final SemiCache semiCache;
    private final SnowFlake snowFlake;
    private final ConfigProperties configProperties;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

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


    @Override
    public List<ObjectNode> query(SqlDefinition sqlDefinition) {
        List<ObjectNode> records = super.query(sqlDefinition);
        Assert.isFalse(records.isEmpty(), () -> new AuthException(AuthResultInfo.AUTHENTICATION));

        ObjectNode objectNode = records.get(0);
        String password = objectNode.get("password").asText();
        JsonNode params = sqlDefinition.getRawParams();
        Assert.isTrue(JwtUtils.bCryptPasswordEncoder.matches(params.get("password").asText(), password), () -> new AuthException(AuthResultInfo.AUTHENTICATION));

        return records;
    }

    @Override
    public void afterQuery(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances, ResultHolder resultHolder) {
        super.afterQuery(sqlDefinition, enhances, resultHolder);
        ObjectNode user = resultHolder.getRecords().get(0);
        String idKey = configProperties.getIdKey();
        Assert.isTrue(user.has(idKey), () -> new ConfigException("登录不能缺少id"));

        Map<String, Object> userMap = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {
        });

        String id = String.valueOf(userMap.remove(idKey));
        semiCache.addHashValue(AuthCacheKey.USER.getKey(), id, user);

        String token = JwtUtils.createToken(id, userMap, authProperties.getTokenDuration());
        HttpServletResponse res = RequestUtils.getResponse();
        res.setHeader(authProperties.getTokenKey(), token);
    }
}

