package com.semifinished.core.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import com.semifinished.core.utils.ParamsValid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * sql的构造器，使用{@link NamedParameterJdbcTemplate}风格的sql，以避免sql注入
 */
public class SqlCreator {

    /**
     * 生成NamedParameterJdbcTemplate风格的insert SQL
     * 格式为 insert table(col1,col2,...) values(:col1,:col2,...)
     *
     * @param table  表名
     * @param fields 字段名
     * @return insert SQL
     */
    public static String insert(String table, Collection<String> fields) {
        String fieldsStr = String.join(",", fields);
        StringBuilder builder = new StringBuilder().append("insert ")
                .append(table)
                .append("(")
                .append(fieldsStr)
                .append(" ) values( ");
        String value = fields.stream().map(s -> ":" + s).collect(Collectors.joining(","));
        builder.append(value).append(")");

        return builder.toString();
    }


    /**
     * 生成NamedParameterJdbcTemplate风格的根据id更新数据SQL
     *
     * @param table  表名
     * @param fields 更新的字段
     * @return update by id
     */
    public static String updateById(String table, Collection<String> fields) {
        return update(table, fields, "id");
    }

    /**
     * 生成NamedParameterJdbcTemplate风格的更新SQL
     *
     * @param table   表名
     * @param fields  更新的字段
     * @param idField 根据该字段更新
     * @return update by idField
     */
    public static String update(String table, Collection<String> fields, String idField) {
        ParamsValid.validAndStr(table, idField);

        StringBuilder builder = new StringBuilder("update ").append(table).append(" set ");
        List<String> setFragment = new ArrayList<>();
        for (String key : fields) {
            if (idField.equals(key) || "id".equals(key)) {
                continue;
            }
            setFragment.add(key + "=:" + key);
        }
        builder.append(String.join(",", setFragment)).append(" where ").append(idField).append("=:").append(idField);

        return builder.toString();
    }

    /**
     * 生成NamedParameterJdbcTemplate风格的根据id删除SQL
     *
     * @param table 表名
     * @return delete by id
     */
    public static String deleteById(String table) {
        return delete(table, "id");
    }

    /**
     * 生成NamedParameterJdbcTemplate风格的根据字段删除SQL
     *
     * @param table 表名
     * @param field 根据该字段删除
     * @return delete by id
     */
    public static String delete(String table, String field) {
        validTable(table);
        ParamsValid.validAndStr(field);
        return "delete from " + table + " where " + field + "=:" + field;

    }

    public static Builder builder() {
        return new Builder();
    }

    public static String count(String table, Set<String> fields) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) count from ").append(table).append(" where 1=1 ");

        for (String field : fields) {
            sql.append(" and ").append(field).append("=:").append(field).append(" ");
        }

        return sql.toString();
    }

    private static void validAndId(Map<String, String> params) {
        String id = params.get("id");
        Assert.hasNotText(id, () -> new ParamsException("参数id错误"));
        ParamsValid.valid(params);
    }

    private static void validTable(String table) {
        if (!StringUtils.hasText(table)) {
            throw new CodeException("sql构建时错误");
        }
    }

    /**
     * 把List<Map<String, Object>>转为SqlParameterSource[]
     *
     * @param params 源数据
     * @return SqlParameterSource[]
     */
    public static SqlParameterSource[] mapToSqlParameterSourceArray(List<? extends Map<String, ?>> params) {
        MapSqlParameterSource[] array = new MapSqlParameterSource[params.size()];
        params.stream().flatMap(m -> m.keySet().stream()).forEach(key -> {
            for (Map<String, ?> param : params) {
                if (!param.containsKey(key)) {
                    param.put(key, null);
                }
            }
        });
        for (int i = 0; i < params.size(); i++) {
            array[i] = new MapSqlParameterSource(params.get(i));
        }
        return array;
    }

    /**
     * 把List<ObjectNode>转为SqlParameterSource[]
     *
     * @param objectNodes 源数据
     * @param fields      转为SqlParameterSource的字段
     * @return SqlParameterSource[]
     */
    public static SqlParameterSource[] toSqlParameterSourceArray(List<ObjectNode> objectNodes, String... fields) {
        if (fields.length == 0) {
            fields = ParamsUtils.fields(objectNodes).toArray(new String[0]);
        }
        MapSqlParameterSource[] array = new MapSqlParameterSource[objectNodes.size()];

        for (int i = 0; i < objectNodes.size(); i++) {
            MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();

            for (String field : fields) {
                ObjectNode objectNode = objectNodes.get(i);
                JsonNode jsonNode = objectNode.get(field);
                if (jsonNode == null) {
                    sqlParameterSource.addValue(field, null);
                } else if (jsonNode.isBoolean()) {
                    sqlParameterSource.addValue(field, jsonNode.asBoolean());
                } else if (jsonNode.isNull()) {
                    sqlParameterSource.addValue(field, null);
                } else {
                    sqlParameterSource.addValue(field, objectNode.get(field).asText());
                }
            }
            array[i] = sqlParameterSource;
        }

        return array;
    }

    public static SqlParameterSource toSqlParameterSource(Map<String, ?> params, String... fields) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (fields.length != 0 && Arrays.stream(fields).noneMatch(f -> f.equals(entry.getKey()))) {
                continue;
            }
            mapSqlParameterSource.addValue(entry.getKey(), entry.getValue());
        }
        return mapSqlParameterSource;
    }

    public static class Builder {
        StringBuilder sql = new StringBuilder();
        String base = "";

        public Builder table(String table, String... fields) {
            String f = "*";
            if (fields.length > 0) {
                f = String.join(",", fields);
            }
            this.base = "select " + f + " from " + table + " where 1=1 ";
            return this;
        }


        public Builder eq(Set<String> data) {
            fragment(data, "=");
            return this;
        }

        public Builder eq(String column) {
            this.sql.append(" and ").append(column).append(" = :").append(column).append(" ");
            return this;
        }

        public Builder in(String field) {
            this.sql.append(" and ").append(field).append(" in (:").append(field).append(")");
            return this;
        }

        private void fragment(Set<String> columns, String operator) {
            StringBuilder sql = new StringBuilder();
            for (String column : columns) {
                sql.append(" and ").append(column).append(" ").append(operator).append(" :").append(column).append(" ");

            }
            this.sql.append(sql);
        }


        public String build() {
            return base + sql.toString();
        }
    }


}
