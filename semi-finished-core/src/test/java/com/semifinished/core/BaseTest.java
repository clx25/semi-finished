package com.semifinished.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(classes = TestApp.class)
@AutoConfigureMockMvc
public class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8) // 设置请求和响应的默认编码为UTF-8
                .build();
    }


    public void Assert(String body, String matchResponse) throws Exception {
        MvcResult enhanceResult = mockMvc.perform(post("/enhance")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String enhanceResponse = enhanceResult.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(enhanceResponse);

        assert jsonNode.has("result") : "响应没有result字段";
        assert matchResponse.equals(jsonNode.get("result").toString()) : "结果不匹配";
    }

    // 从classpath中读取JSON文件内容
    // 方法功能：根据提供的文件路径，尝试从classpath加载文件并返回其字符串内容
    // 如果文件未找到，则抛出IOException异常
    public JsonNode readJsonFileFromClasspath(String filePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException("文件未找到: " + filePath);
            }
            String jsonContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return new ObjectMapper().readTree(jsonContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}