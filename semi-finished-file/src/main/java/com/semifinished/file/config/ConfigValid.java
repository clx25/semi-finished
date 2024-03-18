package com.semifinished.file.config;

import com.semifinished.file.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 对{@link FileProperties}配置进行检查
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConfigValid implements InitializingBean {

    private final FileProperties fileProperties;

    @Override
    public void afterPropertiesSet() {
        String filePath = fileProperties.getFilePath();

        if (!StringUtils.hasText(filePath)) {
            filePath = FileUtil.getDefaultPath();
        }
        log.info("文件路径：" + filePath);

        List<String> imageType = fileProperties.getImageType();
        log.info(imageType == null || imageType.size() == 0 ? "没有配置图片格式" : "配置的图片格式：" + String.join(",", imageType));
    }
}
