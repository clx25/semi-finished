package com.semifinished.scheduling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class CancelCronTaskEnhance implements AfterQueryEnhance {

    private final SchedulingService schedulingService;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();

        Assert.isTrue(records.size() == 1, () -> new ParamsException("定时任务名称错误"));

        ObjectNode objectNode = records.get(0);
        JsonNode rawParams = sqlDefinition.getRawParams();
        //todo 前端停止时传递打断策略
        schedulingService.cancel(objectNode.path("bean_name").asText(), rawParams.path("interrupt").asBoolean());
        records.clear();
    }
}
