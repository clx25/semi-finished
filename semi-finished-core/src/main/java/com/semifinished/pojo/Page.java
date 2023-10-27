package com.semifinished.pojo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分页信息
 */
@Getter
@Setter
public class Page {
    /**
     * 数据总数
     */
    private int total;
    /**
     * 没页数量
     */
    private int pageSize;
    /**
     * 第几页
     */
    private int pageNum;
    /**
     * 是否有上一页
     */
    private boolean hasPre;
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    /**
     * 返回的数据数量
     */
    private int size;
    /**
     * 数据列表
     */
    private List<ObjectNode> records;
}
