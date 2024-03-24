package com.semifinished.core.service.enhance;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.CommonParser;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
@Order(Integer.MIN_VALUE + 500)
public class JsonApiEnhance implements AfterQueryEnhance, AfterUpdateEnhance {
    private final CommonParser commonParser;

    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        ObjectNode mergeParams = commonParser.mergeParams(params,false);
        params.removeAll();
        params.setAll(mergeParams);
        return false;
    }
}



