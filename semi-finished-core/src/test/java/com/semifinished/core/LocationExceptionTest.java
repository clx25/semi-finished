package com.semifinished.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


/**
 * 测试规则在参数中的位置异常
 */
@ActiveProfiles("test")
@SpringBootConfiguration
@SpringBootTest(classes = TestApp.class)
public class LocationExceptionTest {


    @Autowired
    private TestCommon testCommon;

    @Test
    @DisplayName("测试别名规则位置")
    public void aliasLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\":\":\"name:uName\"}}";
        testCommon.testException(params, "别名规则位置错误");
    }

    @Test
    @DisplayName("测试排除规则位置")
    public void excludeLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"~\":\"name\"}}";
        testCommon.testException(params, "排除规则位置错误");
    }

    @Test
    @DisplayName("测试join规则位置")
    public void joinLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"&amp;gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\"}}}";
        testCommon.testException(params, "join规则位置错误");
    }

    @Test
    @DisplayName("测试@row规则位置")
    public void rowNumLocation() {
        //括号
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@row\":1}}";
        testCommon.testException(params, "@row规则位置错误");

        //join规则
        String param2 = "{\"@tb\":\"users\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1}}";
        testCommon.testException(param2, "@row规则位置错误");

        //子查询
        String params3 = "{\"@tb\":{\"@tb\":\"users\",\"@row\":1}}";
        testCommon.testException(params3, "@row规则位置错误");
    }


    @Test
    @DisplayName("测试on规则位置")
    public void onLocation() {
        String params = "{\"@tb\":\"users\",\"@on\":\"id\"}";
        testCommon.testException(params, "@on规则位置错误");

        //on规则在括号规则内
        String params2 = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@row\":1,\"@on\":\"id\"}}";
        testCommon.testException(params2, "@on规则位置错误");

        //on规则在子查询规则内
        String params3 = "{\"@tb\":{\"@tb\":\"users\",\"@row\":1,\"@on\":\"id\"}}";
        testCommon.testException(params3, "@on规则位置错误");
    }


    @Test
    @DisplayName("测试指定列名规则位置")
    public void columnsLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@\":\"id\"}}";
        testCommon.testException(params, "列名规则位置错误");
    }


    @Test
    @DisplayName("测试分页规则位置")
    public void pageLocation() {
        //分页规则在括号规则内
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"pageSize\":10}}";
        testCommon.testException(params, "分页规则位置错误");

        //分页规则在join规则内
        String param2 = "{\"@tb\":\"users\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"pageSize\":10}}";
        testCommon.testException(param2, "分页规则位置错误");

        //分页规则在表字典规则内
        String param3 = "{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1,\"pageSize\":10}}";
        testCommon.testException(param3, "分页规则位置错误");
    }

    @Test
    @DisplayName("测试指定表规则位置")
    public void tbLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@tb\":\"users\"}}";
        testCommon.testException(params, "表名规则位置错误");
    }


    @Test
    @DisplayName("测试子查询group规则位置")
    public void groupLocation() {
        String params = "{\"@tb\":{\"@tb\":\"users\",\"@group\":\"gender\"}}";
        testCommon.testException(params, "子查询中的group字段必须包含查询字段");
    }


    @Test
    @DisplayName("测试内容替换规则位置")
    public void replaceLocation() {
        String params2 = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"#num000\":\"id\"}}";
        testCommon.testException(params2, "内容替换规则位置错误");
    }


    @Test
    @DisplayName("测试排序规则位置")
    public void sortLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"\\\\\": \"id\"} }";
        testCommon.testException(params, "排序规则位置错误");

        String params2 = "{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1,\"\\\\\": \"id\"} }";
        testCommon.testException(params2, "排序规则位置错误");
    }

    @Test
    @DisplayName("测试表名规则位置")
    public void tableLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@tb\": \"users\"} }";
        testCommon.testException(params, "表名规则位置错误");
    }


}
