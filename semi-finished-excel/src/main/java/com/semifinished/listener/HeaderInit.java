package com.semifinished.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.JsonFileUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 初始化excel上传相关配置
 */
//@Component
@AllArgsConstructor
public class HeaderInit implements ApplicationListener<ContextRefreshedEvent> {

    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SemiCache semiCache;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {


        File folder = JsonFileUtils.jarFile(configProperties);

        Map<String, ObjectNode> excel = new HashMap<>();

        if (folder.exists()) {
            parseJsonFile(folder, excel);
        }

        for (File file : JsonFileUtils.classPathFiles(configProperties)) {
            if (file.exists()) {
                parseJsonFile(file, excel);
            }
        }
        Map<String, ObjectNode> value = semiCache.getValue(CoreCacheKey.JSON_CONFIGS.getKey(), "EXCEL");
//        semiCache.addValue(, "POST", excel);
    }

    private void parseJsonFile(File folder, Map<String, ObjectNode> excel) {
        for (ObjectNode objectNode : JsonFileUtils.parseJsonFile(folder, objectMapper)) {
            merge(objectNode, excel);
        }
    }

    private void merge(ObjectNode objectNode, Map<String, ObjectNode> excel) {
        ObjectNode configs = objectNode.with("excel");
        configs.fields().forEachRemaining(entry -> {
            String code = entry.getKey();
            Assert.isTrue(excel.containsKey(code), () -> new ConfigException("excel上传code重复：" + code));
            JsonNode value = entry.getValue();
            Assert.isFalse(value instanceof ObjectNode, () -> new ConfigException("excel上传code格式错误：" + code));
            JsonNode table = value.get("table");
            Assert.isTrue(table == null, () -> new ConfigException("缺少table配置：" + code));
            Assert.isFalse(StringUtils.hasText(table.asText(null)), () -> new ConfigException("table配置不能为空：" + code));
            Assert.isFalse(value.has("header"), () -> new ConfigException("缺少header配置：" + code));
            excel.put("/excel/" + code, (ObjectNode) value);
        });
    }
}
