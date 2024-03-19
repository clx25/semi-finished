package com.semifinished.auth.service.enhance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 登陆后处理，生成token,放入header
 */
@Order(401)
@Component
@AllArgsConstructor
public class AfterLoginEnhance implements AfterQueryEnhance {
    private final ConfigProperties configProperties;
    private final SemiCache semiCache;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {

        ObjectNode user = page.getRecords().get(0);

        String idKey = configProperties.getIdKey();
        Assert.isFalse(user.has(idKey), () -> new ConfigException("登录不能缺少id"));

        Map<String, String> userMap = objectMapper.convertValue(user, new TypeReference<Map<String, String>>() {
        });

        String id = userMap.remove(idKey);
        semiCache.addHashValue(AuthCacheKey.USER.getKey(), id, user);

        String token = JwtUtils.createToken(id, userMap, authProperties.getTokenDuration());
        HttpServletResponse res = RequestUtils.getResponse();
        res.setHeader(authProperties.getTokenKey(), token);
    }
}
