package com.semifinished.core.jdbc;

import com.semifinished.core.exception.ProjectRuntimeException;
import com.semifinished.core.exception.SqlDataException;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.core.utils.ParamsUtils;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SQL执行器，封装了一些常用的查询方法
 */
@RequiredArgsConstructor
public class SqlExecutor {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionManager;
    private final ObjectNodeRowMapper objectNodeRowMapper = new ObjectNodeRowMapper();

    public <R> R transaction(Function<SqlExecutor, R> function) {
        return transactionManager.execute(transactionStatus -> function.apply(this));
    }

    public void transaction(Consumer<SqlExecutor> resultConsumer) {
        transactionManager.executeWithoutResult(transactionStatus -> resultConsumer.accept(this));
    }

    /**
     * 判断是否存在一条数据，如果超过一条，抛出异常
     *
     * @param sql  sql语句
     * @param args sql中的数据
     * @return true：有一条匹配，false：没有匹配
     * @throws SqlDataException 超出一条匹配抛出该异常
     */
    public boolean oneMatch(String sql, Map<String, ?> args) throws SqlDataException {
        ObjectNode objectNode = justOne(sql, args);
        return objectNode != null;
    }

    public boolean existMatch(String sql, Map<String, ?> args) {
        return getOne(sql, args) != null;
    }

    public ObjectNode selectById(String table, String id) {
        List<ObjectNode> list = list(table, MapUtils.of("id", id));
        return list.size() == 0 ? null : list.get(0);
    }

    public ObjectNode getOne(String sql, Map<String, ?> args) {
        List<ObjectNode> list = list(sql, args);
        return list.size() == 0 ? null : list.get(0);
    }

    /**
     * 只查询一个数据，如果超过一个，抛出异常
     *
     * @param sql  sql语句
     * @param args sql中的数据
     * @return 查询出来的一条数据
     * @throws SqlDataException 超出一条数据后抛出的异常
     */
    public ObjectNode justOne(String sql, Map<String, ?> args) throws SqlDataException {
        List<ObjectNode> list = list(sql, args);

        if (list.size() > 1) {
            throw new SqlDataException("找到了" + list.size() + "条数据" + sql + args);
        }
        return list.size() == 0 ? null : list.get(0);
    }


    public List<ObjectNode> list(String sql, Map<String, ?> params) {
        return jdbcTemplate.query(sql, params, objectNodeRowMapper);
    }

    public List<ObjectNode> list(String sql, SqlParameterSource params) {
        return jdbcTemplate.query(sql, params, objectNodeRowMapper);
    }

    public int total(String sql, Map<String, Object> args) {
        Integer count = jdbcTemplate.queryForObject("select count(*) count from (" + sql + ") a", args, Integer.class);
        return count == null ? 0 : count;
    }


    /**
     * 获取这个表的所有数据
     *
     * @param table  表名
     * @param fields 查询的字段，如果length==0，那么获取所有字段
     * @return 表的数据列表
     */
    public List<ObjectNode> list(String table, String... fields) {
        String sql = SqlCreator.builder().table(table, fields).build();
        return jdbcTemplate.query(sql, this::populateData);
    }


    /**
     * 执行sql语句，这个sql返回一个list
     *
     * @param sql 执行的sql语句
     * @return 获取的数据集合
     */
    public List<ObjectNode> list(String sql) {
        return jdbcTemplate.query(sql, objectNodeRowMapper);
    }

    public <T> List<T> list(String sql, Class<T> t) {
        return jdbcTemplate.query(sql, new ObjectNodeDataClassRowMapper<>(t));
    }

    public ObjectNode get(String sql, Map<String, ?> args) {
        return jdbcTemplate.query(sql, args, this::populateOne);
    }

    public ObjectNode get(String sql) {
        return jdbcTemplate.query(sql, this::populateOne);
    }

    private ObjectNode populateOne(ResultSet row) throws SQLException {
        if (row.next()) {
            return populateData(row, 0);
        }
        return null;
    }

    private ObjectNode populateData(ResultSet resultSet, int rowNum) throws SQLException {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            objectNodeRowMapper.ObtainAndPut(resultSet, metaData, objectNode, i);
        }
        return objectNode;
    }


    /**
     * 执行没有返回值的sql
     *
     * @param sql  sql语句
     * @param args sql中的数据
     */
    public void exec(String sql, Map<String, ?> args) {
        jdbcTemplate.update(sql, args);
    }

    public void batch(String sql, List<ObjectNode> list) {
        SqlParameterSource[] sqlParameterSources = SqlCreator.toSqlParameterSourceArray(list);
        jdbcTemplate.batchUpdate(sql, sqlParameterSources);
    }

    /**
     * 批量插入
     *
     * @param table       表名
     * @param objectNodes 数据
     */
    public void batchInsert(String table, List<ObjectNode> objectNodes) {
        if (objectNodes == null || objectNodes.isEmpty()) {
            return;
        }
        Set<String> fields = ParamsUtils.fields(objectNodes);
        fields.remove("id");
        String sql = SqlCreator.insert(table, fields);
        jdbcTemplate.batchUpdate(sql, SqlCreator.toSqlParameterSourceArray(objectNodes));
    }


    /**
     * 批量修改
     *
     * @param table       表名
     * @param objectNodes 数据
     */
    public void batchUpdate(String table, List<ObjectNode> objectNodes) {
        if (objectNodes == null || objectNodes.isEmpty()) {
            return;
        }
        String sql = SqlCreator.updateById(table, ParamsUtils.fields(objectNodes));
        jdbcTemplate.batchUpdate(sql, SqlCreator.toSqlParameterSourceArray(objectNodes));
    }

    /**
     * 批量删除
     *
     * @param table  表名
     * @param field  指定匹配的字段
     * @param values 字段对应的数据
     */
    public void batchDelete(String table, String field, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        SqlParameterSource[] sqlParameterSources = new SqlParameterSource[values.size()];
        int i = 0;
        for (String value : values) {
            MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
            sqlParameterSource.addValue(field, value);
            sqlParameterSources[i++] = sqlParameterSource;
        }

        String sql = SqlCreator.delete(table, field);
        jdbcTemplate.batchUpdate(sql, sqlParameterSources);
    }


    /**
     * 获取所有表名
     *
     * @return 表名列表
     */
    public List<String> tables() {
        return jdbcTemplate.queryForList("show tables", new HashMap<>(), String.class);
    }

    public void update(String sql, Map<String, ?> params) {
        jdbcTemplate.update(sql, params);
    }

    public void delete(String table, String id) {
        String sql = SqlCreator.delete(table, "id");
        jdbcTemplate.update(sql, MapUtils.of("id", id));
    }

    /**
     * 插入数据
     *
     * @param sql    执行的sql
     * @param params sql中对应的参数
     * @return 插入数据的id
     */
    public int insert(String sql, Map<String, ?> params) {
        SqlParameterSource sqlParameterSource = SqlCreator.toSqlParameterSource(params);
        return insert(sql, sqlParameterSource);
    }

    /**
     * 插入数据
     *
     * @param sql                执行的sql
     * @param sqlParameterSource sql中对应的参数
     * @return 插入数据的id
     */
    public int insert(String sql, SqlParameterSource sqlParameterSource) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, sqlParameterSource, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? 0 : key.intValue();
    }

    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public boolean has(String table, Map<String, ?> params) {

        String sql = SqlCreator.count(table, params.keySet());
        Integer integer = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return integer != null && (integer > 0);
    }

    public String getDatabaseProductName() {
        DataSource dataSource = jdbcTemplate.getJdbcTemplate().getDataSource();
        if (dataSource == null) {
            return "";
        }
        try {
            return dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (Exception e) {
            throw new ProjectRuntimeException("获取数据库类型错误", e);
        }
    }
}
