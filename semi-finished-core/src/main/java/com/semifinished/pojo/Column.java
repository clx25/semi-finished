package com.semifinished.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表的字段信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {

    /**
     * 表名
     */
    private String table;
    /**
     * 字段名
     */
    private String column;
    /**
     * 别名
     */
    private String alias;
    /**
     * 字段类型，用于对特定类型的数据进行处理，如时间格式
     */
    private String type;

    /**
     * 导入excel时字段对应的表头
     */
    private String header;

    /**
     * 是否允许查询
     */
//    private String allow;

    /**
     * 是否允许为空
     */
    private boolean nullAble;

    public Column(String table, String column) {
        this.table = table;
        this.column = column;
    }


    public Column(String table, String column, String alias) {
        this.table = table;
        this.column = column;
        this.alias = alias;
    }

    /**
     * 根据表名，列名，别名，判断是否是同一个字段
     * todo调整
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (!table.equals(column.table)) return false;
        if (!this.column.equals(column.column)) return false;
        return alias.equals(column.alias);
    }

    @Override
    public int hashCode() {
        int result = table == null ? 0 : table.hashCode();
        result = 31 * result + (column == null ? 0 : column.hashCode());
        result = 31 * result + (alias == null ? 0 : alias.hashCode());
        return result;
    }
}
