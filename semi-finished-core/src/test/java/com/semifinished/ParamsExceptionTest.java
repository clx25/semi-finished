package com.semifinished;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 测试参数异常
 */
@ActiveProfiles("test")
@DisplayName("参数异常测试")
@SpringBootConfiguration
@SpringBootTest(classes = TestApp.class)
public class ParamsExceptionTest {

    @Autowired
    private TestCommon testCommon;

    @Test
    @DisplayName("未指定表名")
    public void noTable() {
        String params = "{\"id\":\"1\"}";
        testCommon.testException(params, "未指定表名");
    }

    @Test
    @DisplayName("未指定查询字段")
    public void noColumns() {
        String params = "{\"@tb\":\"users\",\"@\":\"\",\"id\":\"1\"}";
        testCommon.testException(params, "未指定查询字段");
    }


    @Test
    @DisplayName("字段名重复")
    public void columnsRepeat() {
        testCommon.testException("{\"@tb\":\"users\",\"@\":\"id,id\",\"id\":\"1\"}", "字段名重复：id");
    }


    @Test
    @DisplayName("字段名为空")
    public void columnsError() {
        testCommon.testException("{\"@tb\":\"users\",\"@\":\",id\",\"id\":\"1\"}", "查询字段错误，字段名不能为空：,id");
    }

    @Test
    @DisplayName("查询字段格式错误")
    public void columnsAliasError() {
//        testCommon.testException("{\"@tb\":\"users\",\"@\":\"id:\",\"id\":\"1\"}", "查询字段错误：id:");
    }
}
