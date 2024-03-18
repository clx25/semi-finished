package com.semifinished.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class JsonFileUtils {


    public static File jarFile(ConfigProperties configProperties) {
        ApplicationHome applicationHome = new ApplicationHome(JsonFileUtils.class);
        File source = applicationHome.getSource();
        if (source == null) {
            source = applicationHome.getDir();
        } else {
            source = source.getParentFile();
        }

        return new File(source, configProperties.getApiFolder());
    }


    public static List<File> classPathFiles(ConfigProperties configProperties) {
        List<File> files = new ArrayList<>();
        try {
            Enumeration<URL> urls = JsonFileUtils.class.getClassLoader().getResources(configProperties.getApiFolder());
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



    public static List<ObjectNode> parseJsonFile(File folder, ObjectMapper objectMapper) {
        File[] files = folder.listFiles((f, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        List<ObjectNode> nodes = new ArrayList<>();
        try {
            for (File file : files) {
                if(file.length()==0){
                    continue;
                }
                JsonNode json = objectMapper.readTree(file);
                Assert.isFalse(json instanceof ObjectNode, () -> new ConfigException(file.getName() + "配置文件格式错误"));
                nodes.add((ObjectNode) json);
            }
        } catch (IOException e) {
            throw new ConfigException(folder + "json配置文件格式错误");
        }
        return nodes;
    }
}
