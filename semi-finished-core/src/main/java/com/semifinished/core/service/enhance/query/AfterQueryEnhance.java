package com.semifinished.core.service.enhance.query;


import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.enhance.ServiceEnhance;

/**
 * 查询的增强，可以在查询的过程中对参数和返回值进行修改
 * todo 添加不使用Enhance的方式，而是使用接口，类似tk.mybatis使用接口和继承通用实现类完成增强功能
 */
public interface AfterQueryEnhance extends ServiceEnhance {


    /**
     * 查询之后执行，无论是否有分页规则，都会包装到page中
     * 可以在此处获取查询后的数据，进行处理
     *
     * @param resultHolder  包含查询返回的数据列表和分页信息
     * @param sqlDefinition SQL定义信息
     */
    default void afterQuery(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
    }

}
