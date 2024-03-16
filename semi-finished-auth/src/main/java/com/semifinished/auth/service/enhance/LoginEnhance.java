package com.semifinished.auth.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录
 */
@Component
@Order(-199)
@AllArgsConstructor
public class LoginEnhance implements AfterQueryEnhance {


    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        Assert.isTrue(records.isEmpty(), () -> new AuthException(AuthResultInfo.AUTHENTICATION));


        ObjectNode objectNode = records.get(0);
        String password = objectNode.get("password").asText();
        JsonNode params = sqlDefinition.getRawParams();
        Assert.isFalse(JwtUtils.bCryptPasswordEncoder.matches(params.get("password").asText(), password), () -> new AuthException(AuthResultInfo.AUTHENTICATION));
    }

}
