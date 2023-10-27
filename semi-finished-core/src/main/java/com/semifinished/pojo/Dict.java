package com.semifinished.pojo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Dict {
    /**
     * 关联主表
     */
    private String table;
    /**
     * 主表关联字段/数据字典code
     */
    private String col;
    /**
     * 字典表
     */
    private String dictTable;
    /**
     * sql查询的字典表关联字段
     */
    private String relationCol;

    /**
     * 代码中匹配数据的实际关联字段
     */
    private String actualRelationCol;
    /**
     * 字典表查询字段,字段->别名
     */
    private List<Pair<String, String>> queryCols;
    /**
     * 排除的字段
     */
    private List<String> excludes;

    public void addExclude(String exclude) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.add(exclude);
    }

    public void addQueryCol(Pair<String, String> pair) {
        if (this.queryCols == null) {
            this.queryCols = new ArrayList<>();
        }
        this.queryCols.add(pair);
    }
}
