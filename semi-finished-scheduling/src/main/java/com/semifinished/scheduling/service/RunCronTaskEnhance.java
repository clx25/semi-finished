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
public class RunCronTaskEnhance implements AfterQueryEnhance {

    private final SchedulingService schedulingService;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();
        if (records.isEmpty()) {
            return;
        }

        ObjectNode objectNode = records.get(0);
        String corn = objectNode.path("cron").asText(null);
        Assert.notBlank(corn, () -> new ParamsException("该任务缺少定时规则"));

        String beanName = objectNode.path("bean_name").asText();
        JsonNode args = objectNode.path("args");
        schedulingService.run(beanName, corn, args);
        records.clear();
    }
}
