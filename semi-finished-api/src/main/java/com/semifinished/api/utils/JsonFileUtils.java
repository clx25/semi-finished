package com.semifinished.api.utils;

import com.semifinished.api.config.ApiProperties;
import com.semifinished.core.exception.ConfigException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class JsonFileUtils {

    /**
     * 根据相对jar包路径获取外部文件夹绝对路径
     *
     * @param apiProperties api文件相对jar包路径
     * @return api json文件所在文件夹
     */
    public static File jarFile(ApiProperties apiProperties) {
        ApplicationHome applicationHome = new ApplicationHome(JsonFileUtils.class);
        File source = applicationHome.getSource();
        if (source == null) {
            source = applicationHome.getDir();
        } else {
            source = source.getParentFile();
        }

        return new File(source, apiProperties.getApiFolder());
    }

    /**
     * 根据相对路径获取类路径
     *
     * @param apiProperties api文件夹类路径相对路径
     * @return api json文件所在文件夹
     */
    public static List<File> classPathFiles(ApiProperties apiProperties) {
        List<File> files = new ArrayList<>();
        try {
            Enumeration<URL> urls = JsonFileUtils.class.getClassLoader().getResources(apiProperties.getApiFolder());
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                if (resource.exists()) {
                    files.add(resource.getFile());
                }
            }
        } catch (IOException e) {
            throw new ConfigException("api文件解析错误");
        }

        return files;
    }


}
