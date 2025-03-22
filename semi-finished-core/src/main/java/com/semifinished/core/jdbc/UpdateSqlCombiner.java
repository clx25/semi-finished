package com.semifinished.core.jdbc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;

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
    public static String updateByIdSQL(SqlDefinition sqlDefinition, String idKey) {

        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();

        Optional<ValueCondition> first = valueConditions.stream().filter(v -> idKey.equals(v.getColumn())).findFirst();
        ValueCondition valueCondition = first.orElseThrow(() -> new ParamsException("参数缺少主键字段：" + idKey));

        Assert.isTrue(valueConditions.size() > 1, () -> new ParamsException("缺少修改数据内容"));

        StringBuilder sql = new StringBuilder(" update ")
                .append(sqlDefinition.getTable())
                .append(" set ");


        String value = valueConditions.stream().filter(v -> !idKey.equals(v.getColumn()))
                .map(v -> "`" + v.getColumn() + "` = :" + v.getArgName())
                .collect(Collectors.joining(" , "));

        sqlDefinition.setId(String.valueOf(valueCondition.getValue()));
        return sql.append(value)
                .append(" where ")
                .append(idKey)
                .append("= :")
                .append(valueCondition.getArgName())
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
        Assert.notEmpty(valueConditions, () -> new ParamsException("缺少新增数据内容"));

        StringJoiner columns = new StringJoiner(",", "(", ")");

        String values = valueConditions.stream()
                .peek(v -> columns.add("`" + v.getColumn() + "`"))
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
     * @param freeDelete    是否自由删除
     * @return 删除语句SQL
     */
    public static String deleteSQL(SqlDefinition sqlDefinition, String idKey, boolean freeDelete) {
        if (freeDelete) {
            return freeDeleteSQL(sqlDefinition);
        }
        ValueCondition valueCondition = getIdArgName(sqlDefinition, idKey);

        return " delete from " + sqlDefinition.getTable() + " where " + idKey + " " + valueCondition.getCondition();
    }

    /**
     * 生成不限制的删除SQL
     *
     * @param sqlDefinition SQL定义信息
     * @return 不限制的删除SQL
     */
    public static String freeDeleteSQL(SqlDefinition sqlDefinition) {
        return "delete from " + sqlDefinition.getTable() + getWhereFragment(sqlDefinition);
    }

    /**
     * 生成限制根据id删除的逻辑删除SQL
     *
     * @param sqlDefinition     SQL定义信息
     * @param idKey             主键字段
     * @param logicDeleteColumn 逻辑删除字段
     * @param freeDelete        是否自由删除
     * @return 根据id删除的逻辑删除SQL
     */
    public static String logicDeleteSQL(SqlDefinition sqlDefinition, String idKey, String logicDeleteColumn, boolean freeDelete) {
        if (freeDelete) {
            return freeLogicDeleteSQL(sqlDefinition, logicDeleteColumn);
        }
        ValueCondition valueCondition = getIdArgName(sqlDefinition, idKey);
        return "update " + sqlDefinition.getTable() + " set " + logicDeleteColumn + " =1 " + " where " + idKey + valueCondition.getCondition();
    }

    /**
     * 生成不限制的逻辑删除SQL
     *
     * @param sqlDefinition     SQL定义信息
     * @param logicDeleteColumn 逻辑删除字段
     * @return 不限制的逻辑删除SQL
     */
    public static String freeLogicDeleteSQL(SqlDefinition sqlDefinition, String logicDeleteColumn) {
        return "update " + sqlDefinition.getTable() + " set " + logicDeleteColumn + " =1 " + getWhereFragment(sqlDefinition);
    }

    /**
     * 生成where条件SQL片段
     *
     * @param sqlDefinition SQL定义信息
     * @return where条件SQL片段
     */
    private static String getWhereFragment(SqlDefinition sqlDefinition) {
        StringBuilder whereFragment = new StringBuilder(" where 1=1 ");
        List<ValueCondition> valueCondition = sqlDefinition.getValueCondition();
        for (ValueCondition w : valueCondition) {
            whereFragment.append(" ")
                    .append(ParamsUtils.hasText(w.getCombination(), "and"))
                    .append(" ")
                    .append(w.getColumn())
                    .append(" ")
                    .append(w.getCondition());
            ;
        }
        return whereFragment.toString();
    }

    /**
     * 获取主键的参数名
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段
     * @return 主键参数名
     */
    private static ValueCondition getIdArgName(SqlDefinition sqlDefinition, String idKey) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        Optional<ValueCondition> first = valueConditions.stream().filter(v -> idKey.equals(v.getColumn())).findFirst();
        ValueCondition valueCondition = first.orElseThrow(() -> new ParamsException("未指定%s的值", idKey));
        Object value = valueCondition.getValue();
        Assert.notBlank(value == null ? null : String.valueOf(value), () -> new ParamsException("%s不能为空", idKey));

        return valueCondition;
    }

    /**
     * 获取修改SQL语句的参数
     *
     * @param sqlDefinition SQL定义信息
     * @return 修改SQL语句的参数
     */
    public static Map<String, ?> getUpdateArgs(SqlDefinition sqlDefinition) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        Map<String, Object> args = new HashMap<>();
        valueConditions.forEach(v -> args.put(v.getArgName(), v.getValue()));
        return args;
    }
}
