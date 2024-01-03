package com.semifinished;

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
}
