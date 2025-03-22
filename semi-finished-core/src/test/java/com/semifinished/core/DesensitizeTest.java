package com.semifinished.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("脱敏测试")
public class DesensitizeTest extends BaseTest {

    JsonNode jsonNode;

    {
        jsonNode = readJsonFileFromClasspath("test/desensitize.json");
    }

    @Test
    @DisplayName("固定长度脱敏")
    public void testDesensitizePhone() throws Exception {
        JsonNode params = jsonNode.get("手机号");
        Assert(params.get("query").toString(), params.get("match").toString());
    }

    @Test
    @DisplayName("比例长度脱敏")
    public void testDesensitizeIdCard() throws Exception {
        JsonNode params = jsonNode.get("身份证");
        Assert(params.get("query").toString(), params.get("match").toString());
    }

    @Test
    @DisplayName("自定义脱敏")
    public void testDesensitizeAddress() throws Exception {
        JsonNode params = jsonNode.get("地址");
        Assert(params.get("query").toString(), params.get("match").toString());
    }
}