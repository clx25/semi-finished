package com.semifinished.core.pojo;

import lombok.Data;

/**
 * 树形结构字段信息
 */
@Data
public class Tree {
    /**
     * 树形结构id字段名
     */
    private String id;
    /**
     * 树形结构父id字段名
     */
    private String parent;

    /**
     * 生成的树形结构子节点字段名
     */
    private String children;
}
