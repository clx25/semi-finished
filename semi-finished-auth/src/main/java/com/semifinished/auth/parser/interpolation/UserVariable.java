package com.semifinished.auth.parser.interpolation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.AuthUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.parser.interpolation.Interpolation;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserVariable implements Interpolation {
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final AuthProperties authProperties;
    private final SemiCache semiCache;

    @Override
    public boolean match(String key, JsonNode interpolatedKey) {
        for (String s : new String[]{"username", "userId", "roleId", "deptId"}) {
            if (s.equals(interpolatedKey.asText(""))) {
                Assert.isFalse(authProperties.isAuthEnable(), () -> new AuthException("需要开启验证并登录后才能使用用户插值规则"));
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonNode value(String table, String key, JsonNode interpolatedKey, SqlDefinition sqlDefinition) {

        String idKey = configProperties.getIdKey();
        String id = RequestUtils.getRequestAttributes(idKey);
        Assert.hasNotText(id, () -> new ParamsException("需要登录后才能使用用户插值规则"));
        ObjectNode user = AuthUtils.getCurrent(semiCache, idKey);
        if (user == null) {

            List<ObjectNode> userInfo = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource()).list("select u.id,u.username,r.role_id,d.dept_id from semi_user u left join semi_user_role r on u.id=r.user_id left join semi_user_dept d on u.id=d.user_id  where u.id=" + id);
            user = JsonNodeFactory.instance.objectNode();
            for (ObjectNode node : userInfo) {
                user.set("userId", node.get(idKey));
                user.set("username", node.get("username"));
                user.withArray("roleId").add(node.get("role_id"));
                user.withArray("deptId").add(node.get("dept_id"));
            }

            AuthUtils.addUser(semiCache, id, user);
        }
        return user.get(interpolatedKey.asText());
    }
}
