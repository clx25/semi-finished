package com.semifinished.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.controller.EnhanceController;
import com.semifinished.core.exception.ParamsException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Component
public class TestCommon {

    @Autowired
    private EnhanceController enhanceController;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void test(String params, int size, String resultEquals) {

        ObjectNode jsonNodes = request(params);
        Assertions.assertThat(jsonNodes.path("code").asText()).isEqualTo("200");
        JsonNode result = jsonNodes.path("result");
        if (size > 0) {
            Assertions.assertThat(result).hasSize(size);
            AssertionsForClassTypes.assertThat(result.path(0).toString()).isEqualTo(resultEquals);
        } else if (size == 0 && StringUtils.hasText(resultEquals)) {
            AssertionsForClassTypes.assertThat(result.toString()).isEqualTo(resultEquals);
        }
    }

    public ObjectNode request(String params) {
        ObjectNode paramsNode;
        try {
            paramsNode = objectMapper.readValue(params, ObjectNode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Object o = enhanceController.queryPostMapping(paramsNode);
        ObjectNode jsonNodes = objectMapper.convertValue(o, ObjectNode.class);
        System.out.println(jsonNodes.toPrettyString());
        return jsonNodes;
    }


    public void testException(String params, String message) {
        assertThatThrownBy(() -> request(params))
                .isInstanceOf(ParamsException.class)
                .hasMessage(message);
    }
}
