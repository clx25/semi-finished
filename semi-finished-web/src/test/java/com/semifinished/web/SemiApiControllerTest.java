package com.semifinished.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc
public class SemiApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetSemiApi() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/semiApi")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    public void testGetSemiApiWithInvalidPageNum() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/semiApi")
                .param("pageNum", "0")  // 小于1
                .param("pageSize", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("页码最小值为1"));
    }

    @Test
    public void testGetSemiApiWithInvalidPageSize() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/semiApi")
                .param("pageNum", "1")
                .param("pageSize", "101"))  // 大于100
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("每页条数最大值为100"));
    }
}
