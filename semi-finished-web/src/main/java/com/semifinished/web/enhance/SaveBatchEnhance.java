package com.semifinished.web.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//@Component
public class SaveBatchEnhance implements AfterUpdateEnhance {
    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();

        String arrayName = null;
        Iterator<String> iterator = params.fieldNames();
        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.startsWith("[") && name.endsWith("]")) {
                arrayName = name;
            }
        }
        if (!StringUtils.hasText(arrayName)) {
            return;
        }
        List<ObjectNode> nodes = new ArrayList<>();

        JsonNode jsonNode = params.get(arrayName);
        params = params.without(arrayName);
        String msg = "参数" + arrayName + "数据类型错误";
        arrayName=arrayName.substring(0,arrayName.length()-1);
        Assert.isTrue(jsonNode instanceof ObjectNode, () -> new ParamsException(msg));
        if (jsonNode instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode)jsonNode;
            for (JsonNode node : arrayNode) {
                params=params.deepCopy();
                params.set(arrayName,node);
                nodes.add(params);
            }
        }

    }
}
