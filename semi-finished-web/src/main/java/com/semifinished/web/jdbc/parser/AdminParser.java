package com.semifinished.web.jdbc.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.paramsParser.ParamsParser;

public class AdminParser implements ParamsParser {
    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
