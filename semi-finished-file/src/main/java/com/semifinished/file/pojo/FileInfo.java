package com.semifinished.file.pojo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class FileInfo {

    /**
     * 文件类型
     */
    @NotEmpty(message = "文件类型不能为空", groups = {checkUpload.class, mergeFile.class})
    private String type;

    /**
     * 完整文件哈希值
     */
    @NotEmpty(message = "文件哈希不能为空", groups = {checkUpload.class, uploadChunk.class, mergeFile.class})
    private String hash;


    /**
     * 分块大小
     */
    @Min(value = 1, message = "分块大小错误", groups = {uploadChunk.class})
    private long chunkSize;

    /**
     * 所有分块的大小
     */
    @Size(min = 1, message = "分块数量不能少于1", groups = {mergeFile.class})
    private long[] chunksSize;

    /**
     * 当前块序号
     */
    @Min(value = 1, message = "当前块序号错误，块序号从1开始", groups = {uploadChunk.class})
    private int chunkNum;


    public interface checkUpload {
    }

    public interface uploadChunk {
    }

    public interface mergeFile {
    }
}
