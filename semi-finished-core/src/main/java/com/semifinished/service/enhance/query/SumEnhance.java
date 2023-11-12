package com.semifinished.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.pojo.Page;
import com.semifinished.util.Assert;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 计算查询出的结果的总和，添加到最后一行
 * <pre>
 *     {
 *         "+":"col1,col2,..."
 *     }
 * </pre>
 */
@Order(100)
@Component
@AllArgsConstructor
public class SumEnhance implements AfterQueryEnhance {

    private final ObjectMapper objectMapper;


    @Override
    public boolean support(SqlDefinition sqlDefinition) {
        return sqlDefinition.getParams().get("+") != null;
    }


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        sum(sqlDefinition, page.getRecords());
    }

    private void sum(SqlDefinition sqlDefinition, List<ObjectNode> records) {
        if (records.isEmpty()) {
            return;
        }
        String fieldStr = sqlDefinition.getRawParams().get("+").asText();
        String[] fields = fieldStr.split(",");
        ObjectNode objectNode = objectMapper.createObjectNode();
        for (String field : fields) {
            BigDecimal sum = new BigDecimal(0);
            for (ObjectNode node : records) {
                JsonNode jsonNode = node.get(field);
                boolean b = jsonNode.isNumber();
                Assert.isFalse(b, () -> new ParamsException(field + "的数据无法进行计算"));
                sum = sum.add(jsonNode.decimalValue());
            }
            objectNode.put(field, sum);
        }
        records.add(objectNode);
    }


}
