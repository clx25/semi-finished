package com.semifinished.core.jdbc;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.utils.Assert;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
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
        StringJoiner sj = new StringJoiner(",");

        valueConditions = valueConditions.stream().filter(v -> !idKey.equals(v.getColumn())).collect(Collectors.toList());
        for (ValueCondition condition : valueConditions) {
            Object value = condition.getValue();

            String apostrophe = value == null ? "" : "'";
            sj.add(condition.getColumn() + " = " + apostrophe + value + apostrophe);
        }

        return sql.append(sj)
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
        StringJoiner values = new StringJoiner(",", "(", ")");
        for (ValueCondition valueCondition : valueConditions) {
            columns.add(valueCondition.getColumn());
            Object value = valueCondition.getValue();
            values.add(value == null ? null : ("'" + value) + "'");
        }

        return " insert into " + sqlDefinition.getTable() + columns + " values" + values;
    }

    /**
     * 生成删除语句SQL
     *
     * @param sqlDefinition SQL定义信息
     * @param idKey         主键字段名
     * @return 删除语句SQL
     */
    public static String deleteSQL(SqlDefinition sqlDefinition, String idKey) {
        List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
        Optional<ValueCondition> first = valueConditions.stream().filter(v -> idKey.equals(v.getColumn())).findFirst();
        ValueCondition valueCondition = first.orElseThrow(() -> new ParamsException("未指定主键的值"));

        Object value = valueCondition.getValue();

        Assert.hasNotText(value == null ? null : String.valueOf(value), () -> new ParamsException("删除的值不能为空"));

        return " delete from " + sqlDefinition.getTable() + " where " + idKey + "='" + value + "'";

    }
}
