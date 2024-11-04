package com.semifinished.core.facotry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;

public interface SqlDefinitionFactory {

    SqlDefinition getSqlDefinition(JsonNode params);
}
