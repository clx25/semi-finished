package com.semifinished.core.jdbc.query;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.jdbc.SqlDefinition;

import java.util.List;

public interface Query {
    /**
     * 查询方言，对应了查询条件中的@dialect规则，如果与该方法返回的字符串相同，那么就使用该对象进行增删查改操作
     *
     * @return 方言
     */
    String dialect();

    /**
     * 查询
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询结果，无论返回单行还是多行都返回集合，通过@row规则处理
     */
    List<ObjectNode> query(SqlDefinition sqlDefinition);

    /**
     * 新增
     *
     * @param sqlDefinition SQL定义信息
     */
    void add(SqlDefinition sqlDefinition);

    /**
     * 修改
     *
     * @param sqlDefinition SQL定义信息
     */
    void update(SqlDefinition sqlDefinition);

    /**
     * 删除
     *
     * @param sqlDefinition SQL定义信息
     */
    void delete(SqlDefinition sqlDefinition);

    /**
     * 执行操作，当需要自定义执行操作时可使用该方法
     *
     * @param sqlDefinition SQL定义信息
     * @return 操作结果
     */
    default <R> R execute(SqlDefinition sqlDefinition) {
        return null;
    }
}