package com.semifinished.web.listener;

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
//@Component
@Order(-1000)
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
                    .filter(c -> columns.stream().noneMatch(node -> node.has(c.getColumn())))
                    .map(c -> {
                        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                        objectNode.put("table_name", c.getTable());
                        objectNode.put("column_name", c.getColumn());
                        objectNode.put("alias", c.getAlias());
                        objectNode.put("allow", !c.isDisabled());
                        return objectNode;
                    })
                    .collect(Collectors.toList());

//
//            List<ObjectNode> add = tablesNode.stream()
//                .filter(t -> columns.stream()
//                        .noneMatch(c -> equals(t, c, "table_name") &&
//                                equals(t, c, "column_name")))
//                .peek(o -> {
//                    o.remove("id");
//                    o.put("allow", 1);
//                })
//                .collect(Collectors.toList());
//
//
//        List<String> remove = columns.stream()
//                .filter(c -> tablesNode.stream()
//                        .noneMatch(t -> equals(t, c, "table_name") &&
//                                equals(t, c, "column_name"))
//                ).map(o -> o.get("id").asText())
//                .collect(Collectors.toList());
//
//
//        sqlExecutor.transaction(executor -> {
//            executor.batchInsert("semi_column", add);
//            executor.batchDelete("semi_column", "id", remove);
//        });
        });
    }
}
