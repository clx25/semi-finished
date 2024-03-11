package com.semifinished.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.AuthUtils;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录
 */
@Component
@Order(-400)
@AllArgsConstructor
public class LoginEnhance implements AfterQueryEnhance {
    private final AuthProperties authProperties;
    private final ConfigProperties configProperties;
    private final SemiCache semiCache;


    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        if (authProperties.isLoginCaptcha() && !AuthUtils.captchaMatch(semiCache, params.path("key").asText(), params.path("captcha").asText())) {
            throw new AuthException(AuthResultInfo.CAPTCHA_ERROR);
        }
    }

    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        Assert.isTrue(records.isEmpty(), () -> new AuthException(AuthResultInfo.AUTHENTICATION));


        ObjectNode objectNode = records.get(0);
        String password = objectNode.get("password").asText();
        JsonNode params = sqlDefinition.getRawParams();
        Assert.isFalse(JwtUtils.bCryptPasswordEncoder.matches(params.get("password").asText(), password), () -> new AuthException(AuthResultInfo.AUTHENTICATION));
        ObjectNode user = JsonNodeFactory.instance.objectNode();

        String id = null;
        String idKey = configProperties.getIdKey();
        for (ObjectNode node : records) {
            id = node.get(idKey).asText();
            user.set(idKey, node.get(idKey));
            user.set("username", node.get("username"));
            user.withArray("role_id").add(node.get("role_id"));
            user.withArray("dept_id").add(node.get("dept_id"));
        }
        AuthUtils.addUser(semiCache, id, user);

        String roleId = records.stream().map(node -> node.path("role_id").asText()).collect(Collectors.joining(","));
        String deptId = records.stream().map(node -> node.path("dept_id").asText()).collect(Collectors.joining(","));
        String token = JwtUtils.createToken(configProperties, params.get("username").asText(), objectNode.get(idKey).asText(), roleId, deptId, authProperties.getTokenDuration());
        HttpServletResponse res = RequestUtils.getResponse();
        res.setHeader(authProperties.getTokenKey(), token);
        JsonNode path = params.path("@");
        if (!path.isMissingNode()) {
            objectNode.retain(path.asText().split(","));
        }
    }

}
