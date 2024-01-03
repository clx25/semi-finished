package com.semifinished;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.controller.EnhanceController;
import com.semifinished.exception.ParamsException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootConfiguration
@SpringBootTest(classes = TestApp.class)
public class ExceptionTest {



    @Autowired
    private TestCommon testCommon;

    @Test
    @DisplayName("测试别名规则位置")
    public void aliasLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\":\":\"name:uName\"}}";
        locationException(params, "别名规则位置错误");
    }

    @Test
    @DisplayName("测试排除规则位置")
    public void excludeLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"~\":\"name\"}}";
        locationException(params, "排除规则位置错误");
    }

    @Test
    @DisplayName("测试join规则位置")
    public void joinLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"&amp;gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\"}}}";
        locationException(params, "join规则位置错误");
    }

    @Test
    @DisplayName("测试@row规则位置")
    public void rowNumLocation() {
        //括号
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@row\":1}}";
        locationException(params, "@row规则位置错误");

        //join规则
        String param2 = "{\"@tb\":\"users\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1}}";
        locationException(param2, "@row规则位置错误");

        //子查询
        String params3 = "{\"@tb\":{\"@tb\":\"users\",\"@row\":1}}";
        locationException(params3, "@row规则位置错误");
    }


    @Test
    @DisplayName("测试on规则位置")
    public void onLocation() {
        String params = "{\"@tb\":\"users\",\"@on\":\"id\"}";
        locationException(params, "@on规则位置错误");

        //on规则在括号规则内
        String params2 = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@row\":1,\"@on\":\"id\"}}";
        locationException(params2, "@on规则位置错误");

        //on规则在子查询规则内
        String params3 = "{\"@tb\":{\"@tb\":\"users\",\"@row\":1,\"@on\":\"id\"}}";
        locationException(params3, "@on规则位置错误");
    }


    @Test
    @DisplayName("测试指定列名规则位置")
    public void columnsLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@\":\"id\"}}";
        locationException(params, "列名规则位置错误");
    }


    @Test
    @DisplayName("测试分页规则位置")
    public void pageLocation() {
        //分页规则在括号规则内
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"pageSize\":10}}";
        locationException(params, "分页规则位置错误");

        //分页规则在join规则内
        String param2 = "{\"@tb\":\"users\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"pageSize\":10}}";
        locationException(param2, "分页规则位置错误");

        //分页规则在表字典规则内
        String param3 = "{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1,\"pageSize\":10}}";
        locationException(param3, "分页规则位置错误");
    }

    @Test
    @DisplayName("测试指定表规则位置")
    public void tbLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@tb\":\"users\"}}";
        locationException(params, "表名规则位置错误");
    }


    @Test
    @DisplayName("测试子查询group规则位置")
    public void groupLocation() {
        String params = "{\"@tb\":{\"@tb\":\"users\",\"@group\":\"gender\"}}";
        locationException(params, "子查询中的group字段必须包含查询字段");
    }


    @Test
    @DisplayName("测试内容替换规则位置")
    public void replaceLocation() {
        String params2 = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"#num000\":\"id\"}}";
        locationException(params2, "内容替换规则位置错误");
    }


    @Test
    @DisplayName("测试排序规则位置")
    public void sortLocation() {
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"\\\\\": \"id\"} }";
        locationException(params, "排序规则位置错误");

        String params2="{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1,\"\\\\\": \"id\"} }";
        locationException(params2,"排序规则位置错误");
    }

    @Test
    @DisplayName("测试表名规则位置")
    public void tableLocation(){
        String params = "{\"@tb\":\"users\",\"name\":{\"value\":\"Alice\",\"@tb\": \"users\"} }";
        locationException(params, "表名规则位置错误");
    }


    public void locationException(String params, String message) {
        assertThatThrownBy(() -> testCommon.request(params))
                .isInstanceOf(ParamsException.class)
                .hasMessage(message);
    }


}
