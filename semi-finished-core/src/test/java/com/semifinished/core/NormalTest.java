package com.semifinished.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.config.DataSourceConfig;
import com.semifinished.core.pojo.Desensitization;
import com.semifinished.core.service.enhance.query.DesensitizeEnhance;
import com.semifinished.core.utils.MapUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


@ActiveProfiles("test")
@SpringBootTest(classes = TestApp.class)
@SpringBootConfiguration
public class NormalTest {


    @Autowired
    private DataSourceConfig dataSourceConfig;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private DesensitizeEnhance desensitizeEnhance;

    @Autowired
    private TestCommon testCommon;

    @Test
    @DisplayName("测试表字典")
    public void testDict() {
        String params = "{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1}}";
        String result = "{\"name\":\"Alice\",\"gender\":\"变性\"}";
        testCommon.test(params, 50, result);
    }

    @Test
    @DisplayName("测试join规则")
    public void testJoin() {
        String params = "{\"@tb\":\"users\",\"[name]\":\"Grace,Henry,Liam,Quinn\",\"~\":\"name\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name\"}}";
        String result = "[{\"id\":\"7\",\"gender\":\"8\",\"name\":\"男跨女\"},{\"id\":\"8\",\"gender\":\"28\",\"name\":\"女性化\"},{\"id\":\"12\",\"gender\":\"14\",\"name\":\"有跨性别经验的男性\"},{\"id\":\"17\",\"gender\":\"8\",\"name\":\"男跨女\"}]";
        testCommon.test(params, 0, result);
    }


    @Test
    @DisplayName("测试指定字段")
    public void testColumn() {
        String params = "{\"@tb\":\"users\",\"@\":\"id,name\",\"@row\":\"1,3\"}";
        String result = "[{\"id\":\"1\",\"name\":\"Alice\"},{\"id\":\"2\",\"name\":\"Bob\"},{\"id\":\"3\",\"name\":\"Charlie\"}]";
        testCommon.test(params, 0, result);

        String params2 = "{\"@tb\":\"users\",\"@row\":\"1,3\"}";
        String result2 = "[{\"id\":\"1\",\"name\":\"Alice\",\"gender\":\"23\"},{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"}]";
        testCommon.test(params2, 0, result2);
    }


    @Test
    @DisplayName("测试分组查询")
    public void testGroup() {
        String params = "{\"@tb\":\"users\",\"@group\":\"gender\",\"@row\":\"1,5\"}";
        String result = "{\"gender\":\"3\",\"id\":[\"49\"],\"name\":[\"William\"]}";
        testCommon.test(params, 5, result);
    }


    @Test
    @DisplayName("测试子查询分组")
    public void testSubTableGroup() {
        String params = "{\"@tb\":{\"@tb\":\"users\",\"@\":\"gender\",\"@group\":\"gender\"}}";
        testCommon.test(params, 0, null);
    }

    @Test
    @DisplayName("测试join分组")
    public void testJoinGroup() {
        String params = "{\"@tb\":\"users\",\"@\":\"\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name\",\"@group\":\"id\"}}";
        testCommon.test(params, 0, null);
    }

    @Test
    @DisplayName("测试聚合函数")
    public void testAggregateFunction() {
        String params = "{\"@tb\":\"users\",\"@\":\"count(*):count,min(id):max\"}";
        String result = "[{\"count\":50,\"max\":\"1\"}]";
        testCommon.test(params, 0, result);


    }


    @Test
    @DisplayName("测试分页规则")
    public void testPage() {
        String params = "{\"@tb\":\"users\",\"pageSize\":\"3\",\"pageNum\":\"2\"}";
        String result = "{\"total\":50,\"pageSize\":3,\"pageNum\":2,\"hasPre\":true,\"hasNext\":true,\"size\":3,\"records\":[{\"id\":\"4\",\"name\":\"David\",\"gender\":\"14\"},{\"id\":\"5\",\"name\":\"Emma\",\"gender\":\"24\"},{\"id\":\"6\",\"name\":\"Frank\",\"gender\":\"40\"}]}";
        testCommon.test(params, 0, result);

        String params2 = "{\"@tb\":\"users\",\"pageNum\":\"2\"}";
        String result2 = "{\"total\":50,\"pageSize\":10,\"pageNum\":2,\"hasPre\":true,\"hasNext\":true,\"size\":10,\"records\":[{\"id\":\"11\",\"name\":\"Kate\",\"gender\":\"24\"},{\"id\":\"12\",\"name\":\"Liam\",\"gender\":\"14\"},{\"id\":\"13\",\"name\":\"Mia\",\"gender\":\"27\"},{\"id\":\"14\",\"name\":\"Noah\",\"gender\":\"25\"},{\"id\":\"15\",\"name\":\"Olivia\",\"gender\":\"14\"},{\"id\":\"16\",\"name\":\"Peter\",\"gender\":\"37\"},{\"id\":\"17\",\"name\":\"Quinn\",\"gender\":\"8\"},{\"id\":\"18\",\"name\":\"Ryan\",\"gender\":\"47\"},{\"id\":\"19\",\"name\":\"Sophia\",\"gender\":\"24\"},{\"id\":\"20\",\"name\":\"Thomas\",\"gender\":\"51\"}]}";
        testCommon.test(params2, 0, result2);
    }


    @Test
    @DisplayName("测试别名")
    public void testAlias() {
        String params = "{\"@tb\":\"users\",\":\":\"name:名字,id:id\",\"@row\":\"1,3\"}";
        String result = "[{\"id\":\"1\",\"gender\":\"23\",\"名字\":\"Alice\"},{\"id\":\"2\",\"gender\":\"30\",\"名字\":\"Bob\"},{\"id\":\"3\",\"gender\":\"15\",\"名字\":\"Charlie\"}]";
        testCommon.test(params, 0, result);

        String params2 = "{\"@tb\":\"users\",\":\":[\"name:名字\",\"id:id\"],\"@row\":\"1,3\"}";
        testCommon.test(params2, 0, result);

        String params3 = "{\"@tb\":\"users\",\":\":\"name:名字,id:id\",\"@row\":\"1,3\"}";
        testCommon.test(params3, 0, result);
    }


    @Test
    @DisplayName("测试等于规则")
    public void testEq() {
        String params = "{\"@tb\":\"users\",\":\":\"name:userName,id:userId\",\"name\":\"Bob\",\"!name\":null,\"@row\":1}";
        String result = "{\"userId\":\"2\",\"userName\":\"Bob\",\"gender\":\"30\"}";
        testCommon.test(params, 0, result);
    }


    @Test
    @DisplayName("测试排除规则")
    public void testExclude() {
        Map<String, Set<String>> excludes = dataSourceConfig.getExcludes();
        dataSourceConfig.setExcludes(MapUtils.of("users", Collections.singleton("id")));
        String params = "{\"@tb\":\"users\",\"~\":\"name\",\"@row\":1}";
        testCommon.test(params, 0, "{\"gender\":\"23\"}");
        dataSourceConfig.setExcludes(excludes);


        String params2 = "{\"@tb\":\"users\",\"@\":\"\",\"~\":\"id\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name,id\"}}";
        testCommon.test(params2, 50, "{\"name\":\"变性\"}");
    }


    @Test
    @DisplayName("测试in规则")
    public void testIn() {
        String params = "{\"@tb\":\"users\",\"[name]\":\"Emma,Ivy,Kate,Emma\"}";
        String paramsArray = "{\"@tb\":\"users\",\"[name]\":[\"Emma\",\"Ivy\",\"Kate\",\"Emma\"]}";
        String result = "[{\"id\":\"5\",\"name\":\"Emma\",\"gender\":\"24\"},{\"id\":\"9\",\"name\":\"Ivy\",\"gender\":\"45\"},{\"id\":\"11\",\"name\":\"Kate\",\"gender\":\"24\"}]";
        testCommon.test(params, 0, result);
        testCommon.test(paramsArray, 0, result);
    }

    @Test
    @DisplayName("测试多个参数in规则")
    public void testInMultiple() {
        String params = "{\"@tb\":\"users\",\"[name,gender]\":\"(Alice,23),(Charlie,15),(Kate,50),(Emma,1)\"}";
        String paramsArray = "{\"@tb\":\"users\",\"[name, gender]\":[\"Alice, 23\",\"Charlie, 15\",\"Kate,50\",\"Emma,1\"]}";
        String result = "[{\"id\":\"1\",\"name\":\"Alice\",\"gender\":\"23\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"}]";
        testCommon.test(params, 0, result);
        testCommon.test(paramsArray, 0, result);
    }


    @Test
    @DisplayName("测试like规则")
    public void testLike() {
        String params = "{\"@tb\":\"users\",\"%name%\":\"na\"}";
        String result = "[{\"id\":\"33\",\"name\":\"Gina\",\"gender\":\"9\"},{\"id\":\"46\",\"name\":\"Tina\",\"gender\":\"34\"},{\"id\":\"50\",\"name\":\"Xena\",\"gender\":\"40\"}]";
        testCommon.test(params, 0, result);
    }


    @Test
    @DisplayName("测试范围查询")
    public void testRange() {

        String paramsRange1 = "{\"@tb\":\"users\",\"<id<\":\"2,4\"}";
        String paramsRange2 = "{\"@tb\":\"users\",\">id>\":\"4,2\"}";
        String result1 = "[{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"}]";
        testCommon.test(paramsRange1, 0, result1);
        testCommon.test(paramsRange2, 0, result1);

        String paramsRange3 = "{\"@tb\":\"users\",\"<id<=\":\"2,4\"}";
        String paramsRange4 = "{\"@tb\":\"users\",\">=id>\":\"4,2\"}";
        String result2 = "[{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"},{\"id\":\"4\",\"name\":\"David\",\"gender\":\"14\"}]";
        testCommon.test(paramsRange3, 0, result2);
        testCommon.test(paramsRange4, 0, result2);

        String paramsRange5 = "{\"@tb\":\"users\",\"<=id<\":\"2,4\"}";
        String paramsRange6 = "{\"@tb\":\"users\",\">id>=\":\"4,2\"}";
        String result3 = "[{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"}]";
        testCommon.test(paramsRange5, 0, result3);
        testCommon.test(paramsRange6, 0, result3);

        String paramsRange7 = "{\"@tb\":\"users\",\"<=id<=\":\"2,4\"}";
        String paramsRange8 = "{\"@tb\":\"users\",\">=id>=\":\"4,2\"}";
        String result4 = "[{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"},{\"id\":\"4\",\"name\":\"David\",\"gender\":\"14\"}]";
        testCommon.test(paramsRange7, 0, result4);
        testCommon.test(paramsRange8, 0, result4);

        String paramsLt1 = "{\"@tb\":\"users\",\"id<\":\"3\"}";
        String Lt1Result = "[{\"id\":\"1\",\"name\":\"Alice\",\"gender\":\"23\"},{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"}]";
        testCommon.test(paramsLt1, 0, Lt1Result);

        String paramsLt2 = "{\"@tb\":\"users\",\"id<=\":\"3\"}";
        String Lt2Result = "[{\"id\":\"1\",\"name\":\"Alice\",\"gender\":\"23\"},{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"}]";
        testCommon.test(paramsLt2, 0, Lt2Result);

        String paramsLg1 = "{\"@tb\":\"users\",\"<id\":\"2\",\"@row\":\"1,3\"}";
        String lg1Result = "[{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"},{\"id\":\"4\",\"name\":\"David\",\"gender\":\"14\"},{\"id\":\"5\",\"name\":\"Emma\",\"gender\":\"24\"}]";
        testCommon.test(paramsLg1, 0, lg1Result);

        String paramsLg2 = "{\"@tb\":\"users\",\"<=id\":\"2\",\"@row\":\"1,3\"}";
        String lg2Result = "[{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"30\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"15\"},{\"id\":\"4\",\"name\":\"David\",\"gender\":\"14\"}]";
        testCommon.test(paramsLg2, 0, lg2Result);


    }


    @Test
    @DisplayName("测试排序")
    public void testSort() {
        String params = "{\"@tb\":\"users\",\"/\":\"gender\",\"@row\":\"1,4\"}";
        String result = "[{\"id\":\"20\",\"name\":\"Thomas\",\"gender\":\"51\"},{\"id\":\"41\",\"name\":\"Oliver\",\"gender\":\"50\"},{\"id\":\"48\",\"name\":\"Vera\",\"gender\":\"48\"},{\"id\":\"18\",\"name\":\"Ryan\",\"gender\":\"47\"}]";
        testCommon.test(params, 0, result);

        String params2 = "{\"@tb\":\"users\",\"\\\\\":\"gender\",\"@row\":\"1,4\"}";
        String result2 = "[{\"id\":\"49\",\"name\":\"William\",\"gender\":\"3\"},{\"id\":\"47\",\"name\":\"Usher\",\"gender\":\"5\"},{\"id\":\"44\",\"name\":\"Rachel\",\"gender\":\"6\"},{\"id\":\"40\",\"name\":\"Nathan\",\"gender\":\"7\"}]";
        testCommon.test(params2, 0, result2);

    }


    @Test
    @DisplayName("测试字段与表名映射")
    public void mapping() {
        DataSourceConfig.Mapping temp = dataSourceConfig.getMapping();
        DataSourceConfig.Mapping mapping = new DataSourceConfig.Mapping();
        mapping.setEnable(true);
        mapping.setTable(MapUtils.of("users", "uuu"));
        mapping.setColumn(MapUtils.of("users", MapUtils.of("name", "userName")));
        dataSourceConfig.setMapping(mapping);
        String params = "{\"@tb\":\"uuu\",\"userName\":\"Xena\"}";
        String result = "[{\"id\":\"50\",\"userName\":\"Xena\",\"gender\":\"40\"}]";
        testCommon.test(params, 0, result);
        dataSourceConfig.setMapping(temp);
        mapping.getTable().clear();
        mapping.getColumn().clear();

        String params2 = "{\"@tb\":\"users\",\"name\":\"Xena\"}";
        String result2 = "[{\"id\":\"50\",\"name\":\"Xena\",\"gender\":\"40\"}]";
        testCommon.test(params2, 0, result2);
    }


    @Test
    @DisplayName("测试括号查询与或查询")
    public void testBrackets() {
        String params = "{\"@tb\":\"users\",\"%name\":{\"value\":\"a\",\"|%name\":\"e\"},\"gender>\":35}";
        String result = "[{\"id\":\"48\",\"name\":\"Vera\",\"gender\":\"48\"},{\"id\":\"50\",\"name\":\"Xena\",\"gender\":\"40\"}]";
        testCommon.test(params, 0, result);
    }


    @Test
    @DisplayName("测试子表查询")
    public void testSubTable() {
        String params = "{\"@tb\":{\"@tb\":\"users\",\":\":\"name:userName\",\"pageSize\":4},\"@\":\"userName:uName,gender\"}";
        String result = "[{\"uName\":\"Alice\",\"gender\":\"23\"},{\"uName\":\"Bob\",\"gender\":\"30\"},{\"uName\":\"Charlie\",\"gender\":\"15\"},{\"uName\":\"David\",\"gender\":\"14\"}]";
        testCommon.test(params, 0, result);
    }


    @Test
    @DisplayName("测试插值规则")
    public void testInterpolation() {
        String params = "{\"@tb\":\"users\",\"id$\":\"random\",\"@row\":1}";
        ObjectNode objectNode = testCommon.request(params);
        JsonNode result = objectNode.path("result");
        ObjectNode objectNode2 = testCommon.request(params);
        JsonNode result2 = objectNode2.path("result");
        Assertions.assertThat(result.path("id").asText()).isNotEmpty().isNotEqualTo(result2.path("id").asText());
    }


    @Test
    @DisplayName("测试数据替换规则")
    public void testReplace() {
        String params = "{\"@tb\":\"users\",\"#num000\":\"id\",\"#boolean\":\"gender\",\"&id\":{\"@tb\":\"user_order\",\"@on\":\"user_id\",\"@\":\"order_date,money\",\"#timeyyyy/MM/dd\":\"order_date\",\"#def0.000\":\"money\",\"\\\\\":\"money\",\"id=\":\"6\"}}";
        String result = "{\"id\":\"024\",\"name\":\"Xavier\",\"gender\":true,\"order_date\":\"2023/06/05\",\"money\":\"0.000\"}";
        testCommon.test(params, 1, result);

        String params2 = "{\"@tb\":{\"@tb\":\"users\",\"#num000\":\"id\"}}";
        testCommon.test(params2, 50, "{\"id\":\"001\",\"name\":\"Alice\",\"gender\":\"23\"}");

        String params3 = "{\"@tb\":\"users\",\"@\":\"name\",\"gender:\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\",\"@row\":1,\"#num000\":\"id\"}}";
        testCommon.test(params3, 50, "{\"name\":\"Alice\",\"gender\":\"变性\"}");
    }


    @Test
    @DisplayName("测试时间数据格式化")
    public void testDateTimeReplace() {
        String params = "{\"@tb\":\"user_order\",\"id\":\"1\",\"#timeyyyy/MM/dd\":\"order_date\"}";
        testCommon.test(params, 0, null);

        String params2 = "{\"@tb\":\"user_order\",\"id\":\"8\",\"#timeyyyy/MM/dd\":\"order_date\"}";
        testCommon.test(params2, 0, null);
    }


    @Test
    @DisplayName("测试转boolean数据")
    public void testBooleanReplace() {
        String params = "{\"@tb\":\"user_order\",\"id\":\"8\",\"#boolean\":\"order_date\"}";
        testCommon.test(params, 0, "[{\"id\":\"8\",\"user_id\":\"45\",\"order_date\":false,\"money\":\"80.50\"}]");


        String params2 = "{\"@tb\":\"users\",\"id\":\"11\",\"@\":\"name\",\"id:\":{\"@tb\":\"user_order\",\"@on\":\"user_id\",\"@\":\"money\",\"#boolean\":\"money\"}}";
        testCommon.test(params2, 0, "[{\"name\":\"Kate\",\"money\":[true,true,true,true,true]}]");

        String params3 = "{\"@tb\":\"menu\",\"id\":\"1\",\"@\":\"id,label,route\",\"#boolean\":\"route\"}\n";
        String result = "[{\"id\":\"1\",\"label\":\"Home\",\"route\":true}]";
        testCommon.test(params3, 0, result);
    }

    @Test
    @DisplayName("测试json数据格式化")
    public void testJsonReplace() {
        String params = "{\"@tb\":\"menu\",\"id\":\"1\",\"#json\":\"icon\"}";
        String result = "[{\"id\":\"1\",\"label\":\"Home\",\"icon\":{\"type\":\"chart-graph\",\"size\":24,\"strokeWidth\":2,\"theme\":\"outline\",\"fill\":[\"#0045f1\"]},\"route\":true}]";
        testCommon.test(params, 0, result);

        String params2 = "{\"@tb\":\"menu\",\"id\":\"4\",\"#json\":\"icon\"}";
        String result2 = "[{\"id\":\"4\",\"label\":\"Role\",\"icon\":null,\"route\":false}]";
        testCommon.test(params2, 0, result2);
    }

    @Test
    @DisplayName("测试脱敏")
    public void testDesensitization() {

//        Desensitization custom = Desensitization.builder().table("users")
//                .column("name")
//                .desensitize((name) -> TextNode.valueOf("a-" + name.asText() + "-c"))
//                .build();
//        Desensitization builtin = Desensitization.builder().table("gender").column("name").left(1).right(1).build();
//
        List<Desensitization> desensitizes = desensitizeEnhance.getDesensitizes();
//        desensitizes.add(custom);
//        desensitizes.add(builtin);
//
//        String params = "{\"@tb\":\"users\",\"@\":\"id,name\",\"&gender\":{\"@tb\":\"gender\",\"@on\":\"id\",\"@\":\"name:gender\"},\"pageSize\":\"3\"}";
//        String result = "{\"total\":50,\"pageSize\":3,\"pageNum\":1,\"hasPre\":false,\"hasNext\":true,\"size\":3,\"records\":[{\"id\":\"1\",\"name\":\"a-Alice-c\",\"gender\":\"变性\"},{\"id\":\"2\",\"name\":\"a-Bob-c\",\"gender\":\"同*恋\"},{\"id\":\"3\",\"name\":\"a-Charlie-c\",\"gender\":\"经**********性\"}]}";
//        testCommon.test(params, 0, result);
//
//        Desensitization percentage = Desensitization.builder().table("gender").column("name").left(0.2).right(0.2).build();
//        desensitizes.clear();
//        desensitizes.add(percentage);
//
//        String result2 = "{\"total\":50,\"pageSize\":3,\"pageNum\":1,\"hasPre\":false,\"hasNext\":true,\"size\":3,\"records\":[{\"id\":\"1\",\"name\":\"Alice\",\"gender\":\"**\"},{\"id\":\"2\",\"name\":\"Bob\",\"gender\":\"***\"},{\"id\":\"3\",\"name\":\"Charlie\",\"gender\":\"经历********男性\"}]}";
//        testCommon.test(params, 0, result2);

        Desensitization nullValue = Desensitization.builder().table("menu").column("icon").left(1).right(1).build();
        desensitizes.clear();
        desensitizes.add(nullValue);
        String param2 = "{\"@tb\":\"menu\",\"id\":\"4\"}";
        testCommon.test(param2, 0, "[{\"id\":\"4\",\"label\":\"Role\",\"icon\":null,\"route\":false}]");

    }


    @Test
    @DisplayName("测试树结构")
    public void testTree() {
        String params = "{\"@tb\":\"role\",\"@\":\"code,name_cn\",\"^\":{\"id\":\"id\",\"parent\":\"parent_id\"}}";
        String result = "[{\"code\":\"0001\",\"name_cn\":\"CEO\",\"children\":[{\"code\":\"0002\",\"name_cn\":\"CTO\",\"children\":[{\"code\":\"0006\",\"name_cn\":\"技术总监\",\"children\":[{\"code\":\"0007\",\"name_cn\":\"软件开发部经理\",\"children\":[{\"code\":\"0009\",\"name_cn\":\"软件工程师\"}]},{\"code\":\"0008\",\"name_cn\":\"硬件开发部经理\",\"children\":[{\"code\":\"0010\",\"name_cn\":\"硬件工程师\"}]}]}]},{\"code\":\"0003\",\"name_cn\":\"CFO\"},{\"code\":\"0004\",\"name_cn\":\"人力资源总监\"}]}]";
        testCommon.test(params, 0, result);
    }

}
