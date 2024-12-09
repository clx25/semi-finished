package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ToCamelEnhance implements AfterQueryEnhance{

    private final ConfigProperties configProperties;
    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();

    }
}
