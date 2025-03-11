package com.semifinished.auth.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.jdbc.SqlCreator;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.parser.paramsParser.ParamsParser;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

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
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        Object id = RequestUtils.getRequestAttributes("id");
        if(id==null){
            return;
        }
        String roleId = RequestUtils.getRequestAttributes("roleId");
        Assert.isFalse(roleId == null, () -> new CodeException("缺少角色信息"));
        String[] roleIds = roleId.split(",");
        String sql = SqlCreator.builder()
                .table("semi_role")
                .in("id")
                .eq("code")
                .build();
        Map<String, Object> args = MapUtils.of("id", Arrays.asList(roleIds), "code", authProperties.getAdminCode());
        boolean existed = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .existMatch(sql, args);
        deepParse(params, existed);
    }


    private void deepParse(JsonNode params, boolean existed) {

        if (params instanceof ArrayNode) {
            for (JsonNode jsonNode : params) {
                deepParse(jsonNode, existed);
            }
        }

        if (!(params instanceof ObjectNode)) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = params.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            String key = next.getKey();
            JsonNode value = next.getValue();
            if (key.startsWith("?")) {
                fields.remove();
                if (!existed) {
                    key = key.substring(1);
                    ((ObjectNode) params).set(key, value);
                }
                return;
            }
            if (!(value instanceof ValueNode)) {
                deepParse(value, existed);
            }
        }

    }


    @Override
    public int getOrder() {
        return -1300;
    }
}
