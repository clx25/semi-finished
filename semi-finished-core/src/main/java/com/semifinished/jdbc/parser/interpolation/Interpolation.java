package com.semifinished.jdbc.parser.interpolation;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.jdbc.SqlDefinition;

/**
 * 用于$插值规则
 * "name$":"username"
 * 如上的请求规则，表示username是一个变量，需要寻找对应的实际值形成
 * "name":"实际值" 的请求参数
 * 会执行第一个匹配到的类，并执行value方法中获取实际值，其他不会执行
 */
public interface Interpolation {
    /**
     * 匹配请求参数，判断是否执行该插值规则
     *
     * @param key             请求参数的key，已经去除末尾的$符号
     * @param interpolatedKey 插值key
     * @return true表示使用该类获取实际值，false表示不使用
     */
    boolean match(String key, String interpolatedKey);

    /**
     * 获取变量对应的实际值
     *
     * @param table           表名
     * @param interpolatedKey 插值key
     * @param key             请求参数的key，已经去除末尾的$符号
     * @param sqlDefinition   SQL定义信息
     * @return 变量对应的实际值
     */
    JsonNode value(String table, String key, String interpolatedKey, SqlDefinition sqlDefinition);
}