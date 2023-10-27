package com.semifinished.service.enhance;


import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Page;

/**
 * 查询的增强，可以在查询的过程中对参数和返回值进行修改
 */
public interface SelectEnhance extends ServiceEnhance {


    /**
     * 查询之后执行
     *
     * @param page          分页信息，包含查询返回的数据列表
     * @param sqlDefinition 解析完成后的sqlDefinition
     */
    default void afterQuery(Page page, SqlDefinition sqlDefinition) {

    }
}
