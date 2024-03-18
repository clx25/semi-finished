package com.semifinished.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("semi-finished.file")
public class FileProperties {
    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 图片上传支持的文件类型
     */
    private List<String> imageType;
}
