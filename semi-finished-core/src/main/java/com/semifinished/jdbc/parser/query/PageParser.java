package com.semifinished.jdbc.parser.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.config.ConfigProperties;
import com.semifinished.jdbc.SqlDefinition;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 分页参数解析
 */

@Component
@AllArgsConstructor
public class PageParser implements ParamsParser {

    private final ConfigProperties configProperties;

    /**
     * 解析分页规则，分页规则的key从配置中获取
     * 只要pageSize或pageNum有一个有数据，那么另外一个没有数据的会设置一个默认数据，pageSize默认10，pageNum默认1
     *
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {

        JsonNode pageNumNode = params.remove(configProperties.getPageNumKey());

        JsonNode pageSizeNode = params.remove(configProperties.getPageSizeKey());

        if (pageNumNode == null && pageSizeNode == null) {
            sqlDefinition.setMaxPageSize(configProperties.getMaxPageSize());
            return;
        }

        if (pageNumNode != null) {
            sqlDefinition.setPageNum(pageNumNode.asInt(10));
        } else {
            sqlDefinition.setPageNum(1);
        }

        if (pageSizeNode != null) {
            sqlDefinition.setPageSize(pageSizeNode.asInt(1));
        } else {
            sqlDefinition.setPageSize(10);
        }
    }

    @Override
    public int getOrder() {
        return -700;
    }
}
