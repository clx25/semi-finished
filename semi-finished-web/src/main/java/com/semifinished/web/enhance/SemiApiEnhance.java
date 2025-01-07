package com.semifinished.web.enhance;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SemiApiEnhance implements AfterQueryEnhance {
    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return supportForBeanName(sqlDefinition);
    }

    @Override
    public void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();
        
    }
}
