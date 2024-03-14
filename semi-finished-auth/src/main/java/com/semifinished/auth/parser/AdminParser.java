package com.semifinished.auth.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.ParamsParser;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * 请求参数的?表示如果该请求是管理员，那么忽略这条规则
 * <pre>
 *     {
 *         "?id>":"5"
 *     }
 * </pre>
 * 如上规则，如果不是管理员，那么生成id>5的查询，如果是，那么该规则不生效
 */
@Component
@AllArgsConstructor
public class AdminParser implements ParamsParser {
    private final AuthProperties authProperties;


    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        String roleCode = RequestUtils.getRequestAttributes("roleCode");

        deepParse(params, params.deepCopy(), roleCode);
    }

    private void deepParse(ObjectNode rawParams, JsonNode params, String roleCode) {

        if (params instanceof ArrayNode) {
            for (JsonNode jsonNode : params) {
                deepParse(rawParams, jsonNode, roleCode);
            }
        }

        if (!(params instanceof ObjectNode)) {
            return;
        }

        params.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (!(value instanceof ValueNode)) {
                deepParse(rawParams, value, roleCode);
            }

            if (!key.startsWith("?")) {
                return;
            }
            Assert.isTrue(roleCode == null, () -> new CodeException("缺少角色信息"));
            rawParams.remove(key);

            boolean match = Arrays.stream(roleCode.split(",")).anyMatch(code -> Objects.equals(code, authProperties.getAdminCode()));
            if (!match) {
                key = key.substring(1);

                rawParams.set(key, value);
            }
        });
    }


    @Override
    public int getOrder() {
        return -1300;
    }
}
