package com.semifinished.web.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.listener.RefreshCacheApplication;
import com.semifinished.core.pojo.Column;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(-800)
@AllArgsConstructor
public class SemiColumnListener implements ApplicationListener<RefreshCacheApplication> {

    private final SemiCache semiCache;
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public void onApplicationEvent(RefreshCacheApplication event) {
        sqlExecutorHolder.getSqlExecutorMap().forEach((dataSourceName, executor) -> {
            List<Column> columnList = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + dataSourceName);
            List<ObjectNode> columns = executor.list("select id,table_name,column_name from semi_column");
            List<ObjectNode> add = columnList.stream()
                    .filter(c -> columns.stream()
                            .noneMatch(node -> c.getTable().equals(node.get("table_name").asText())
                                    && c.getColumn().equals(node.get("column_name").asText())))
                    .map(c -> {
                        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                        objectNode.put("table_name", c.getTable());
                        objectNode.put("column_name", c.getColumn());
                        objectNode.put("alias", c.getAlias());
                        objectNode.put("allow", !c.isDisabled());
                        return objectNode;
                    })
                    .collect(Collectors.toList());


            List<String> remove = columns.stream()
                    .filter(node -> columnList.stream()
                            .noneMatch(c -> c.getTable().equals(node.get("table_name").asText())
                                    && c.getColumn().equals(node.get("column_name").asText())))
                    .map(c -> c.get("id").asText())
                    .collect(Collectors.toList());


            executor.batchInsert("semi_column", add, "id");
            executor.batchDelete("semi_column", "id", remove);

        });
    }
}
