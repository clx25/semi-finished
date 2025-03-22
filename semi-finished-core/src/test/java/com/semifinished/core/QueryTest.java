package com.semifinished.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QueryTest extends BaseTest{

    JsonNode jsonNode;

    {
        jsonNode = readJsonFileFromClasspath("test/query.json");
    }
    @Test
    @Order(1)
    @DisplayName("get查询")
    public void getQuery() throws Exception {

        JsonNode update = jsonNode.get("查询");

        JsonNode params = update.get("query");

        MvcResult mvcResult = mockMvc.perform(get("/enhance")
                        .contentType("application/json")
                        .param("@tb", params.get("@tb").asText())
                        .param("[id]", params.get("[id]").asText()))
                .andExpect(status().isOk())
                .andReturn();

        String enhanceResponse = mvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(enhanceResponse);

        assert jsonNode.has("result") : "响应没有result字段";
        assert update.get("match").asText().equals(jsonNode.get("result").asText()) : "结果不匹配";
    }
}
