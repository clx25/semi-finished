package com.semifinished.core;

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
    @DisplayName("时间格式化规则错误")
    public void testDateTimeReplace() {
        String params = "{\"@tb\":\"user_order\",\"id\":\"1\",\"#time\":\"order_date\"}";
        testCommon.testException(params, "缺少时间格式化规则：time");

        String params2 = "{\"@tb\":\"user_order\",\"id\":\"1\",\"#timeabc\":\"order_date\"}";
        testCommon.testException(params2, "时间格式化规则错误：timeabc");
    }


    @Test
    @DisplayName("数字格式化规则错误")
    public void testNumberReplace() {
        String params = "{\"@tb\":\"user_order\",\"id\":\"1\",\"#num\":\"money\"}";
        testCommon.testException(params, "缺少数字格式化规则：num");

        String params2 = "{\"@tb\":\"user_order\",\"id\":\"1\",\"#num#0,000.##0\":\"money\"}";
        testCommon.testException(params2, "数字格式化规则错误：num#0,000.##0");
    }

    @Test
    @DisplayName("json格式化规则错误")
    public void testJsonReplace() {
        String params = "{\"@tb\":\"menu\",\"id\":\"4\",\"#json\":\"id\"}";
        testCommon.testException(params, "json规则执行失败");
    }


    @Test
    @DisplayName("插值规则错误")
    public void testInterpolation() {
        String params = "{\"@tb\":\"menu\",\"id\":\"4\",\"id$\":\"abc\"}";
        testCommon.testException(params, "插值规则未找到对应的值：id$");
    }


    @Test
    @DisplayName("指定字段错误")
    public void testColumn() {
        String params = "{\"@tb\":\"menu\",\"@\":\"abc\"}";
        testCommon.testException(params, "参数错误：abc");
    }


    @Test
    @DisplayName("树结构参数错误")
    public void testTree() {
        String params = "{\"@tb\":\"role\",\"@\":\"code,name_cn\",\"^\":{\"id\":\"id\",\"parent\":\"parent_id\",\"children\":\"\"}}\n";
        testCommon.testException(params, "树查询children值不能为空");

        String params2 = "{\"@tb\":\"role\",\"@\":\"code,name_cn\",\"id:\":{\"@tb\":\"user_role\",\"@on\":\"role_id\",\"@\":\"user_id\",\"^\":{\"children\":\"children\"}},\"^\":{\"id\":\"id\",\"parent\":\"parent_id\",\"children\":\"children\"}}";
        testCommon.testException(params2, "树查询规则重复：children");
    }


}
