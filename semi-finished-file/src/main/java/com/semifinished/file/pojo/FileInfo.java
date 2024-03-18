package com.semifinished.file.pojo;

import lombok.Data;

@Data
public class FileInfo {

    /**
     * 文件类型
     */
    private String type;

    /**
     * 完整文件哈希值
     */
    private String hash;

    /**
     * 完整文件的大小
     */
    private long size;

    /**
     * 第一个块大小
     */
    private long firstSize;

    /**
     * 最后一个块大小
     */
    private long lastSize;

    /**
     * 块总数
     */
    private int chunkSize;

    /**
     * 当前块序号
     */
    private int chunk;

}
