package com.semifinished.jdbc;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用ObjectNode包装数据库查询结果
 */
@Component
public class ObjectNodeRowMapper implements RowMapper<ObjectNode> {
    private final Map<String, ResultConsumer> obtain;

    {
        obtain = new HashMap<>();
        obtain.put("DECIMAL", this::putBigDecimal);
        obtain.put("DECIMAL UNSIGNED", this::putBigDecimal);
        obtain.put("TINYINT", this::putInteger);
        obtain.put("TINYINT UNSIGNED", this::putInteger);
        obtain.put("BOOLEAN", this::putBoolean);
        obtain.put("SMALLINT", this::putInteger);
        obtain.put("SMALLINT UNSIGNED", this::putInteger);
        obtain.put("INT", this::putInteger);
        obtain.put("INT UNSIGNED", this::putLong);
        obtain.put("FLOAT", this::putDouble);
        obtain.put("FLOAT UNSIGNED", this::putDouble);
        obtain.put("DOUBLE", this::putDouble);
        obtain.put("DOUBLE UNSIGNED", this::putDouble);
        obtain.put("BIT", this::putBoolean);
        obtain.put("BIGINT", this::putLong);
        obtain.put("BIGINT UNSIGNED", this::putLong);
        obtain.put("MEDIUMINT", this::putInteger);
        obtain.put("MEDIUMINT UNSIGNED", this::putInteger);
        obtain.put("VARBINARY", this::putByteArray);
        obtain.put("BINARY", this::putByteArray);
//        obtain.put("TIMESTAMP", this::putString);
//        obtain.put("DATE", this::putString);
//        obtain.put("TIME", this::putString);
//        obtain.put("DATETIME", this::putString);
//        obtain.put("YEAR", this::putString);
//        obtain.put("VARCHAR", this::putString);
//        obtain.put("NULL", this::putString);
//        obtain.put("JSON", this::putString);
//        obtain.put("ENUM", this::putString);
//        obtain.put("SET", this::putString);
//        obtain.put("TINYBLOB", this::putString);
//        obtain.put("TINYTEXT", this::putString);
//        obtain.put("MEDIUMBLOB", this::putString);
//        obtain.put("MEDIUMTEXT", this::putString);
//        obtain.put("LONGBLOB", this::putString);
//        obtain.put("LONGTEXT", this::putString);
//        obtain.put("BLOB", this::putString);
//        obtain.put("TEXT", this::putString);
//        obtain.put("CHAR", this::putString);


//        map.put("GEOMETRY", (Class) null);
//        map.put("UNKNOWN", (Class) null);
    }


    @Override
    public ObjectNode mapRow(ResultSet rs, int rowNum) throws SQLException {

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            ObtainAndPut(rs, metaData, objectNode, i);
        }
        return objectNode;
    }

    public void ObtainAndPut(ResultSet resultSet, ResultSetMetaData metaData, ObjectNode objectNode, int i) throws SQLException {

        String columnLabel = metaData.getColumnLabel(i);
        ResultConsumer resultConsumer = obtain.get(metaData.getColumnTypeName(i));
        Object object = resultSet.getObject(columnLabel);
        if (object == null) {
            objectNode.putNull(columnLabel);
            return;
        }
        if (resultConsumer == null) {
            putString(objectNode, columnLabel, resultSet);
            return;
        }
        resultConsumer.accept(objectNode, columnLabel, resultSet);
    }


    public void putString(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getString(columns));
    }

    private void putInteger(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getInt(columns));
    }

    private void putDouble(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getDouble(columns));
    }

    private void putBigDecimal(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getBigDecimal(columns));
    }

    private void putLong(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getLong(columns));
    }

    private void putBoolean(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getBoolean(columns));
    }

    private void putByteArray(ObjectNode objectNode, String columns, ResultSet row) throws SQLException {
        objectNode.put(columns, row.getBytes(columns));
    }


    @FunctionalInterface
    private interface ResultConsumer {
        void accept(ObjectNode objectNode, String columns, ResultSet row) throws SQLException;
    }
}
