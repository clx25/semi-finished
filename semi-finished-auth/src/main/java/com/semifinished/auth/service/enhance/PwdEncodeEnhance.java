package com.semifinished.auth.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码
 */
@Order(-180)
@Component
public class PwdEncodeEnhance implements AfterUpdateEnhance {

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        JsonNode password = params.get("password");
        String encode = bCryptPasswordEncoder.encode(password.asText());
        params.set("password", TextNode.valueOf(encode));
    }
}
