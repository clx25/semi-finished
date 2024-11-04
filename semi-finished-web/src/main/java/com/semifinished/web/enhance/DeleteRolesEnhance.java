package com.semifinished.web.enhance;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.MapUtils;
import org.springframework.stereotype.Component;

@Component
public class DeleteRolesEnhance implements AfterUpdateEnhance {

    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
//        ArrayNode roleIdNode = sqlDefinition.getParams().withArray("roleId");


    }

    @Override
    public void transactional(SqlAutoExecutor executor, SqlDefinition sqlDefinition) {
//        String userId = sqlDefinition.getRawParams().get("userId").asText();
//        executor.update("delete from semi_user_role where user_id=:userId", MapUtils.of("userId", userId));
    }
}
