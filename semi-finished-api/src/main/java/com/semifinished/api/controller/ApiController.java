package com.semifinished.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@CrossOrigin
@RestController
@RequiredArgsConstructor
public class ApiController {
    private Map<String, String> methodContentType = new HashMap<>();
    private final SemiCache semiCache;

    {
        methodContentType.put("POST", "application/json");
        methodContentType.put("PUT", "application/json");
    }


    @GetMapping("api")
    public ObjectNode api() {
        return createApiDoc();
    }

    /**
     * 创建api文档
     * 创建openapi3.0格式的json文档，可以直接由swagger ui解析
     * <a href="https://petstore.swagger.io/"/>
     */
    public ObjectNode createApiDoc() {
        Map<String, Map<String, ObjectNode>> apiMap = semiCache.getValue(CoreCacheKey.JSON_CONFIGS.getKey());

        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("openapi", "3.0.3");
        ObjectNode infoNode = objectNode.with("info");
        infoNode.put("title", "SEMI-FINISHED接口文档");
        infoNode.put("version", "1.0");


        ObjectNode paths = objectNode.with("paths");

        for (Map<String, ObjectNode> m : apiMap.values()) {
            m.forEach((path, configs) -> {
                ObjectNode pathNode = paths.with(path);
                ArrayNode methodNodes = configs.withArray("method");
                ObjectNode methodNode = pathNode.with(methodNodes.get(0).asText().toLowerCase());
                ArrayNode tagsNode = methodNode.withArray("tags");
                tagsNode.add(configs.get("tag").asText());
                methodNode.put("summary", configs.path("summary").asText());

                ObjectNode responsesNode = methodNode.with("responses");
                responsesNode.with("200").put("description", "操作成功");

                String contentType = methodContentType.get(methodNodes.get(0).asText());
                if (contentType == null) {
                    parameters(methodNode, configs, path);
                    return;
                }
                ObjectNode requestBodyNode = methodNode.with("requestBody");
                ObjectNode contentNode = requestBodyNode.with("content");
                ObjectNode contentTypeNode = contentNode.with(contentType);
                ObjectNode schemaNode = contentTypeNode.with("schema");
                boolean batch = configs.has("@batch");
                if (batch) {
                    putArray(schemaNode, configs);
                } else {
                    putObject(schemaNode, configs);
                }

            });
        }
        return objectNode;
    }

    private void parameters(ObjectNode methodNode, ObjectNode configs, String path) {
        ArrayNode parametersNodes = methodNode.withArray("parameters");

        Set<String> fields = new HashSet<>();
        populateFields(configs, fields);
        ObjectNode parametersNode = parametersNodes.addObject();
        JsonNode ruler = configs.path("ruler");
        for (String field : fields) {
            parametersNode.put("name", field);
            parametersNode.put("in", "query");
            description(parametersNode, ruler, field);
        }
        Set<String> pathParameters = getPathParameters(path);
        for (String field : pathParameters) {
            parametersNode.put("name", field);
            parametersNode.put("in", "path");
            description(parametersNode, ruler, field);
        }
    }

    private Set<String> getPathParameters(String path) {
        Set<String> variables = new HashSet<>();

        // 使用正则表达式匹配大括号内的内容
        Pattern pattern = Pattern.compile("\\{([^/]+)}");
        Matcher matcher = pattern.matcher(path);

        while (matcher.find()) {
            // 添加每个匹配到的占位符名称
            variables.add(matcher.group(1));
        }

        return variables;
    }


    private void putObject(ObjectNode schemaNode, ObjectNode configs) {
        schemaNode.put("type", "object");
        populateProperties(schemaNode, configs);

    }

    private void putArray(ObjectNode schemaNode, ObjectNode configs) {
        schemaNode.put("type", "array");
        ObjectNode itemsNode = schemaNode.with("items");
        populateProperties(itemsNode, configs);
    }

    private void populateProperties(ObjectNode node, ObjectNode configs) {
        node.put("type", "object");
        ObjectNode propertiesNode = node.with("properties");
        Set<String> fields = new HashSet<>();
        populateFields(configs, fields);
        JsonNode ruler = configs.path("ruler");
        for (String field : fields) {
            ObjectNode fieldNode = propertiesNode.with(field);
            description(fieldNode, ruler, field);
        }
    }

    private void description(ObjectNode parametersNode, JsonNode ruler, String field) {
        JsonNode fieldRuler = ruler.path(field);
        if (!fieldRuler.isMissingNode()) {
            StringJoiner joiner = new StringJoiner(",");
            if (fieldRuler.has("desc")) {
                joiner.add(fieldRuler.path("desc").asText(""));
            } else {
                fieldRuler.elements().forEachRemaining(n -> joiner.add(n.asText()));
            }

            parametersNode.put("description", joiner.toString());
            if (fieldRuler.has("required")) {
                parametersNode.put("required", true);
            }
        }
        parametersNode.with("schema").put("type", "string");
    }

    private void populateFields(ObjectNode configs, Set<String> fields) {
        configs.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (key.endsWith("$$")) {
                fields.add(key.substring(0, key.length() - 2));
                return;
            }

            if (value instanceof ObjectNode) {
                populateFields((ObjectNode) value, fields);
                return;
            }

            if (!(value instanceof ArrayNode)) {
                return;
            }
            for (JsonNode jsonNode : value) {
                if (jsonNode instanceof ObjectNode) {
                    populateFields((ObjectNode) jsonNode, fields);
                }
            }
        });
    }


}
