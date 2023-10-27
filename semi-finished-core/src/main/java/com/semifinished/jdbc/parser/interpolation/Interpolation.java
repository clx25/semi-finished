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
     * 匹配变量名称
     *
     * @param key 变量名称
     * @return true表示使用该类获取实际值，false表示不使用
     */
    boolean match(String key);

    /**
     * 获取变量对应的实际值
     *
     * @param table         表名
     * @param key           变量名称
     * @param sqlDefinition sql的定义文件，所有sql相关的数据保存到里面，根据这些数据生成sql
     * @return 变量对应的实际值
     */
    JsonNode value(String table, String key, SqlDefinition sqlDefinition);
}
