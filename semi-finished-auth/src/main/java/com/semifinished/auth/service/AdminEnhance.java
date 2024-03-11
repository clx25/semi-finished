package com.semifinished.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.utils.AuthUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 管理员规则
 * 当key的第一个字符是?，表示如果当前的账号是管理员，该条件不生效
 * "?key":"value"
 */
@Order(Integer.MIN_VALUE + 50)
@Component
@AllArgsConstructor
public class AdminEnhance implements AfterQueryEnhance {
    private final SemiCache semiCache;
    private final AuthProperties authProperties;
    private final ConfigProperties configProperties;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        parse(sqlDefinition.getParams(), AuthUtils.getCurrent(semiCache, configProperties.getIdKey()));
        return false;
    }

    private void parse(ObjectNode params, ObjectNode current) {
        params.fieldNames().forEachRemaining(key -> {
            if (!key.startsWith("?")) {
                return;
            }
            key = key.substring(1);
            JsonNode value = params.remove(key);

            ArrayNode arrayNode = current.withArray("role_code");
            for (JsonNode jsonNode : arrayNode) {
                if (!authProperties.getAdminCode().equals(jsonNode.asText())) {
                    continue;
                }
                if (value instanceof ObjectNode) {
                    parse((ObjectNode) value, current);
                }
                params.set(key, value);
                return;
            }
        });
    }


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {

    }
}
