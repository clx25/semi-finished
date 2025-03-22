package com.semifinished.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



public class UpdateTest extends BaseTest {
    JsonNode jsonNode;

    {
        jsonNode = readJsonFileFromClasspath("test/update.json");
    }

    @Test
    @Order(1)
    @DisplayName("新增数据")
    public void add() throws Exception {


        JsonNode update = jsonNode.get("新增");
        mockMvc.perform(post("/common")
                        .contentType("application/json")
                        .content(update.get("update").toString()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }

    @Test
    @Order(2)
    @DisplayName("修改数据")
    public void update() throws Exception {

        JsonNode update = jsonNode.get("修改");
        mockMvc.perform(put("/common")
                        .contentType("application/json")
                        .content(update.get("update").toString()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }

    @Test
    @Order(3)
    @DisplayName("删除数据")
    public void remove() throws Exception {
        JsonNode update = jsonNode.get("删除");
        JsonNode params = update.get("update");

        mockMvc.perform(delete("/enhance")
                        .param("@tb", params.get("@tb").asText())
                        .param("id", params.get("id").asText()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }


    @Test
    @Order(4)
    @DisplayName("批量新增数据")
    public void batchAdd() throws Exception {


        JsonNode update = jsonNode.get("批量新增");
        mockMvc.perform(post("/common")
                        .contentType("application/json")
                        .content(update.get("update").toString()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }

    @Test
    @Order(5)
    @DisplayName("批量编辑数据")
    public void batchUpdate() throws Exception {

        JsonNode update = jsonNode.get("批量修改");
        mockMvc.perform(put("/common")
                        .contentType("application/json")
                        .content(update.get("update").toString()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }


    @Test
    @Order(6)
    @DisplayName("批量删除数据")
    public void batchDelete() throws Exception {

        JsonNode update = jsonNode.get("批量删除");
        JsonNode params = update.get("update");
        mockMvc.perform(delete("/enhance")
                        .contentType("application/json")
                        .param("@tb", params.get("@tb").asText())
                        .param("[id]", params.get("[id]").asText()))
                .andExpect(status().isOk());

        Assert(update.get("query").toString(), update.get("match").toString());
    }


}
