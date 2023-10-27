package com.semifinished.service.enhance.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.SelectEnhance;
import com.semifinished.util.Assert;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 把结果转为树结构
 */
@Order(0)
@Component
public class TreeEnhance implements SelectEnhance {


    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        if (params.get("pageSize") == null &&
                params.get("pageNum") == null) {
            return params.get("^") != null;
        }
        return false;
    }

    @Override
    public void beforeParse(SqlDefinition sqlDefinition) {
        ObjectNode params = sqlDefinition.getParams();
        JsonNode jsonNode = params.get("^");
        JsonNode colJsonNode = params.get("@");
        if (colJsonNode == null) {
            return;
        }
        //如果在指定的字段中没有指定转为树结构需要的参数，那么需要加上，再在after中删除
        Set<String> columns = Arrays.stream(colJsonNode.asText().split(",")).collect(Collectors.toSet());
        columns.add(getValue(jsonNode, "parent"));
        columns.add(getValue(jsonNode, "id"));
        params.put("@", String.join(",", columns));
    }


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        toTree(sqlDefinition.getRawParams(), page.getRecords());
    }


    private void toTree(JsonNode params, List<ObjectNode> records) {
        if (records.isEmpty()) {
            return;
        }
        JsonNode jsonNode = params.get("^");

        String parent = getValue(jsonNode, "parent");
        String children = getValue(jsonNode, "children");
        String id = getValue(jsonNode, "id");
        List<String> delKeys = new ArrayList<>();
        JsonNode colJsonNode = params.get("@");
        if (colJsonNode != null) {
            String[] columns = colJsonNode.asText().split(",");
            if (!Arrays.asList(columns).contains(parent)) {
                delKeys.add(parent);
            }
            if (!Arrays.asList(columns).contains(id)) {
                delKeys.add(id);
            }
        }

        List<ObjectNode> treeTopList = records.stream().filter(objectNode -> isTop(objectNode, parent)).map(ObjectNode::deepCopy).collect(Collectors.toList());

        populateTree(treeTopList, records, parent, children, id, delKeys);
        records.clear();
        records.addAll(treeTopList);

    }

    private String getValue(JsonNode params, String key) {
        JsonNode jsonNode = params.get(key);
        String value = jsonNode == null ? key : jsonNode.asText();
        Assert.hasNotText(value, () -> new ParamsException("树查询" + key + "值不能为空"));
        return value;
    }

    private boolean isTop(ObjectNode objectNode, String parent) {
        JsonNode jsonNode = objectNode.get(parent);
        Assert.isNull(jsonNode, () -> new ParamsException("参数" + parent + "错误"));
        return jsonNode.asInt() == 0;
    }

    public void populateTree(List<ObjectNode> parentList, List<ObjectNode> allNodes, String parent, String children, String id, List<String> delKeys) {
        for (ObjectNode objectNode : parentList) {
            List<ObjectNode> list = allNodes.stream()
                    .filter(node -> objectNode.get(id).asText().equals(node.get(parent).asText()))
                    .map(ObjectNode::deepCopy)
                    .collect(Collectors.toList());

            for (String delKey : delKeys) {
                objectNode.remove(delKey);
            }
            if (list.isEmpty()) {
                continue;
            }
            ArrayNode arrayNode = objectNode.putArray(children);
            arrayNode.addAll(list);
            populateTree(list, allNodes, parent, children, id, delKeys);
        }
    }
}
