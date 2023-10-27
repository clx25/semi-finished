package com.semifinished.service.enhance;


import com.semifinished.jdbc.SqlDefinition;

/**
 * 提供功能更强的增强，如：需要改变数据结构，改变返回格式
 */
public interface SelectFinallyEnhance {

    /**
     * 查询数据的处理，每个查询只会执行一次
     * 该接口返回的数据不会经过任何处理，直接返回到页面
     *
     * @param data          查询出来的数据，可能是集合也可能是分页数据
     * @param sqlDefinition 请求参数解析后的数据
     * @return 返回到页面的数据
     */
    Object beforeReturn(Object data, SqlDefinition sqlDefinition);

}
