package com.semifinished.core.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 把{@link SqlDefinition}中的数据组合成新增，修改，删除SQL
 */
public class UpdateSqlCombiner {

    /**
     * 生成修改语句SQL
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段名
     * @return 修改语句SQL
     */
    public static String updateSQL(SqlDefinition sqlDefinition, String idKey) {

        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();

        Optional<ValueCondition> first = valueConditions.stream().filter(v -> idKey.equals(v.getColumn())).findFirst();
        ValueCondition valueCondition = first.orElseThrow(() -> new ParamsException("参数缺少主键字段：" + idKey));

        Assert.isFalse(valueConditions.size() > 1, () -> new ParamsException("缺少修改数据内容"));

        StringBuilder sql = new StringBuilder(" update ")
                .append(sqlDefinition.getTable())
                .append(" set ");


        String value = valueConditions.stream().filter(v -> !idKey.equals(v.getColumn()))
                .map(v -> v.getColumn() + " = :" + v.getArgName())
                .collect(Collectors.joining(" , "));


        return sql.append(value)
                .append(" where ")
                .append(idKey)
                .append("='")
                .append(valueCondition.getValue())
                .append("'")
                .toString();

    }

    /**
     * 生成新增语句SQL，并排除参数中的主键字段
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段名
     * @return 新增语句SQL
     */
    public static String addSQLExcludeId(SqlDefinition sqlDefinition, String idKey) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();

        valueConditions = valueConditions.stream().filter(v -> !idKey.equals(v.getColumn())).collect(Collectors.toList());
        Assert.isEmpty(valueConditions, () -> new ParamsException("缺少新增数据内容"));

        StringJoiner columns = new StringJoiner(",", "(", ")");

        String values = valueConditions.stream()
                .peek(v -> columns.add(v.getColumn()))
                .map(v -> ":" + v.getArgName())
                .collect(Collectors.joining(",", "(", ")"));


        return " insert into " + sqlDefinition.getTable() + columns + " values" + values;
    }

    /**
     * 获取批量操作的参数
     *
     * @param sqlDefinition SQL定义信息
     * @param objectMapper  序列化库
     * @return 批量操作的参数
     */
    public static Map<String, Object>[] getBatchArgs(SqlDefinition sqlDefinition, ObjectMapper objectMapper) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        ArrayNode batch = sqlDefinition.getExpand().withArray("@batch");
        batch.forEach(node -> {
            ObjectNode objectNode = (ObjectNode) node;
            for (ValueCondition v : valueConditions) {
                JsonNode value = objectNode.remove(v.getColumn());
                objectNode.set(v.getArgName(), value);
            }
        });

        return objectMapper.convertValue(batch, new TypeReference<Map<String, Object>[]>() {
        });
    }

    /**
     * 生成删除语句SQL
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段名
     * @return 删除语句SQL
     */
    public static String deleteSQL(SqlDefinition sqlDefinition, String idKey) {
        String value = getIdArgName(sqlDefinition, idKey);
        return " delete from " + sqlDefinition.getTable() + " where " + idKey + "=':" + value + "'";
    }

    /**
     * 生成逻辑删除SQL
     *
     * @param sqlDefinition     SQL定义信息
     * @param idKey             主键字段
     * @param logicDeleteColumn 逻辑删除字段
     * @return 逻辑和三处SQL
     */
    public static String logicDeleteSQL(SqlDefinition sqlDefinition, String idKey, String logicDeleteColumn) {
        String value = getIdArgName(sqlDefinition, idKey);
        return "update " + sqlDefinition.getTable() + " set " + logicDeleteColumn + " =1 " + " where " + idKey + "=':" + value + "'";
    }

    /**
     * 获取主键的参数名
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段
     * @return 主键参数名
     */
    private static String getIdArgName(SqlDefinition sqlDefinition, String idKey) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        Optional<ValueCondition> first = valueConditions.stream().filter(v -> idKey.equals(v.getColumn())).findFirst();
        ValueCondition valueCondition = first.orElseThrow(() -> new ParamsException("未指定%s的值", idKey));
        Object value = valueCondition.getValue();
        Assert.hasNotText(value == null ? null : String.valueOf(value), () -> new ParamsException("%s不能为空", idKey));

        return valueCondition.getArgName();
    }

    /**
     * 获取修改SQL语句的参数
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键
     * @return 修改SQL语句的参数
     */
    public static Map<String, ?> getUpdateArgs(SqlDefinition sqlDefinition, String idKey) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        Map<String, Object> args = new HashMap<>();
        valueConditions.stream().filter(v -> !idKey.equals(v.getColumn()))
                .forEach(v -> args.put(v.getArgName(), v.getValue()));
        return args;
    }
}
