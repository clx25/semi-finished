package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.utils.Assert;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 把结果转为树结构
 * <pre>
 *     {
 *         "^":{
 *             "id":"id",
 *             "parent":"parent",
 *             "children":"children"
 *         }
 *     }
 *     id: 当前数据标识字段
 *     parent: 用来寻找父元素的字段，如果a数据的parent字段的数据与b数据的id相等，那么表示b数据是a数据的父元素
 *     children: 下一个层级的key
 * </pre>
 */
@Order(200)
@Component
public class TreeEnhance implements AfterQueryEnhance {


    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        List<ObjectNode> expandAll = QuerySqlCombiner.expandAll(sqlDefinition);
        Set<String> repeat = new HashSet<>();
        StringJoiner sj = new StringJoiner(",");
        boolean needToTree = false;
        for (ObjectNode expand : expandAll) {
            JsonNode tree = expand.path("^");
            if (tree.isMissingNode()) {
                continue;
            }
            needToTree = true;
            tree.fieldNames().forEachRemaining(key -> {
                if (!repeat.add(key)) {
                    sj.add(key);
                }
            });
        }
        if (!needToTree) {
            return;
        }
        Assert.hasText(sj.toString(), () -> new ParamsException("树查询规则重复：" + sj));
    }

    @Override
    public void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();
        if (records.isEmpty()) {
            return;
        }


        List<ObjectNode> expandAll = QuerySqlCombiner.expandAll(sqlDefinition);
        ObjectNode treeConfig = JsonNodeFactory.instance.objectNode();
        boolean needToTree = false;
        for (ObjectNode expand : expandAll) {
            JsonNode tree = expand.path("^");
            if (tree.isMissingNode()) {
                continue;
            }
            needToTree = true;
            tree.fields().forEachRemaining(entry -> treeConfig.put(entry.getKey(), entry.getValue().asText()));
        }
        if (!needToTree) {
            return;
        }
        toTree(treeConfig, records);
    }


    /**
     * 转树结构
     *
     * @param treeConfig 树结构的配置字段
     * @param records    数据集合
     */
    private void toTree(ObjectNode treeConfig, List<ObjectNode> records) {

        String parent = treeConfig.path("parent").asText("parent");
        String children = treeConfig.path("children").asText("children");
        String id = treeConfig.path("id").asText("id");


        //由于支持join规则，所以树结构的id和parent字段可能为空，这里需要两个都获取作为顶层数据
        //并且深拷贝一次，使树结构数据与查询结果数据分离
        List<ObjectNode> treeTopList = records.stream()
                .filter(objectNode -> isTop(objectNode, parent) || isTop(objectNode, id))
                .map(ObjectNode::deepCopy)
                .collect(Collectors.toList());

        //排除id和parent字段为空的行，减少转树结构时的判断次数，同时避免由于关联字段不存在可能产生的空指针异常
        List<ObjectNode> allNodes = records.stream()
                .filter(node -> !isTop(node, id))
                .filter(node -> !isTop(node, parent))
                .collect(Collectors.toList());

        covertTree(treeTopList, allNodes, parent, children, id);
        records.clear();
        records.addAll(treeTopList);

    }

    /**
     * 判断是不是树结构顶层的数据
     *
     * @param objectNode 判断的数据
     * @param field      判断的字段
     * @return true 是顶层数据，false 不是顶层数据
     */
    private boolean isTop(ObjectNode objectNode, String field) {
        JsonNode jsonNode = objectNode.path(field);
        if (jsonNode instanceof NumericNode) {
            return jsonNode.asDouble() == 0;
        }
        return !StringUtils.hasText(jsonNode.asText(null));
    }

    /**
     * 转树结构
     *
     * @param parentList 转换后的树结构数据
     * @param allNodes   需要转换的数据集合
     * @param parent     判断父元素的标识字段
     * @param children   下一层结构的字段
     * @param id         当前数据的标识字段
     */
    public void covertTree(List<ObjectNode> parentList, List<ObjectNode> allNodes, String parent, String children, String id) {
        for (ObjectNode objectNode : parentList) {
            List<ObjectNode> list = allNodes.stream()
                    .filter(node -> objectNode.path(id).asText("").equals(node.get(parent).asText()))
                    .map(ObjectNode::deepCopy)
                    .collect(Collectors.toList());


            if (list.isEmpty()) {
                continue;
            }
            ArrayNode arrayNode = objectNode.putArray(children);
            arrayNode.addAll(list);
            covertTree(list, allNodes, parent, children, id);
        }
    }
}
