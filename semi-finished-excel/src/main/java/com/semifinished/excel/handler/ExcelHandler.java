package com.semifinished.excel.handler;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * excel数据处理器
 * todo 添加默认处理与其他处理混合，事务等处理
 */
public interface ExcelHandler {

    /**
     * excel数据处理
     *
     * @param configs 表名
     * @param rows    excel解析后的数据
     * @param header  表头数据
     */
    void handle(ObjectNode configs, ArrayNode rows, Map<Integer, String> header);


    /**
     * excel请求path
     * 该path会匹配请求的servletPath，使匹配的处理器生效
     * 如果返回null或者空字符串表示匹配所有excel请求
     *
     * @return 编号
     */
    String[] path();
}
