package com.semifinished.core.service.enhance.query;


import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.service.enhance.ServiceEnhance;

/**
 * 查询的增强，可以在查询的过程中对参数和返回值进行修改
 */
public interface AfterQueryEnhance extends ServiceEnhance {


    /**
     * 查询之后执行，无论是否有分页规则，都会包装到page中
     * 可以在此处获取查询后的数据，进行处理
     *
     * @param page          分页信息，包含查询返回的数据列表
     * @param sqlDefinition SQL定义信息
     */
    default void afterQuery(Page page, SqlDefinition sqlDefinition) {
    }

}
