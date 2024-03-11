package com.semifinished.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.AuthUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SignupEnhance implements AfterUpdateEnhance {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final AuthProperties authProperties;
    private final SemiCache semiCache;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        if (authProperties.isSignupCaptcha() &&
                !AuthUtils.captchaMatch(semiCache, params.path("key").asText(), params.path("captcha").asText())) {
            throw new AuthException(AuthResultInfo.CAPTCHA_ERROR);
        }

        JsonNode password = params.path("password");

        for (ValueCondition valueCondition : sqlDefinition.getValueCondition()) {
            if ("password".equals(valueCondition.getColumn())) {
                String encode = bCryptPasswordEncoder.encode(password.asText());
                valueCondition.setValue(encode);
                return;
            }
        }

    }

}
