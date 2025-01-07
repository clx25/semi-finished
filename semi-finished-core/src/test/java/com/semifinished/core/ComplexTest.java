package com.semifinished.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootConfiguration
@SpringBootTest(classes = TestApp.class)
public class ComplexTest {

    @Autowired
    private TestCommon testCommon;

    @Test
    public void test() {
        String params = "{\"@tb\":{\"@tb\":\"users\",\"pageSize\":20,\"~\":\"gender\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\"}},\"id:\":{\"@tb\":\"user_order\",\"@\":\"order_date,money\",\"@on\":\"user_id\",\"#num0.00\":\"money\"}}";
        testCommon.request(params);
    }

    @Test
    public void test2() {
        String params = "{\"@tb\":\"users\",\"~\":\"id\",\"id:\":{\"@tb\":\"user_role\",\"@on\":\"user_id\",\"@\":\"\",\"role_id:\":{\"@tb\":\"role\",\"@on\":\"id\",\"@\":\"code,name_cn\",\"@row\":0}}}";
        testCommon.test(params, 50, "{\"name\":\"Alice\",\"gender\":\"23\",\"code\":\"0001\",\"name_cn\":\"CEO\"}");
    }


    @Test
    public void test3() {
        String params = "{\"@tb\":\"users\",\"~\":\"id\",\"id:\":{\"@tb\":\"user_role\",\"@on\":\"user_id\",\"@\":\"\",\"role_id:\":{\"@tb\":\"role\",\"@on\":\"id\",\"@row\":1,\"^\":{\"id\":\"id\",\"parent\":\"parent_id\"}}}}";
        testCommon.request(params);
    }
}
