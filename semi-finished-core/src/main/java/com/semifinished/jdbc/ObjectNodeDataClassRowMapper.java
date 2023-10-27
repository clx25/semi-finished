package com.semifinished.jdbc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link DataClassRowMapper}会根据实体类中字段类型去获取数据
 * 当实体类中有ObjectNode数据时，{@link JdbcUtils#getResultSetValue}中没有直接获取ObjectNode格式数据
 * 的方法，所以在debug模式下会打印异常信息，当前类就是把ObjectNode类型改为为String类型
 * 再通过{@link StringToObjectNodeConverter}把从数据库中查询到的字符串转为ObjectNode
 */
public class ObjectNodeDataClassRowMapper<T> extends DataClassRowMapper<T> {
    public ObjectNodeDataClassRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    public Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        Class<?> propertyType = pd.getPropertyType();
        if (propertyType == ObjectNode.class) {
            propertyType = String.class;
        }
        return JdbcUtils.getResultSetValue(rs, index, propertyType);
    }
}
