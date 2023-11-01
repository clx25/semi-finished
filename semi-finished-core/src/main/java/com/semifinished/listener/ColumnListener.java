package com.semifinished.listener;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.cache.SemiCache;
import com.semifinished.constant.CoreCacheKey;
import com.semifinished.jdbc.SqlExecutorHolder;
import com.semifinished.pojo.Column;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 对semi_column表数据进行初始化
 * todo 多数据源支持
 */
@Order(-1000)
@Component
@AllArgsConstructor
public class ColumnListener implements ApplicationListener<RefreshCacheApplication> {
    private final SemiCache semiCache;
    private final SqlExecutorHolder sqlExecutorHolder;

    /**
     * 对semi_column表中的数据进行梳理
     * 把项目的表结构保存到缓存中
     *
     * @param event ContextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(RefreshCacheApplication event) {
        sqlExecutorHolder.getSqlExecutorMap().forEach((key, executor) -> {
            String dataSource = key.replace("sqlExecutor", "");
            ObjectNode objectNode = executor.get("select database() db");
            String db = objectNode.get("db").asText();

            List<Column> tableList = semiCache.getValue(CoreCacheKey.COLUMNS.getKey() + dataSource);
            if (tableList == null) {
                tableList = new ArrayList<>();
            }
            List<Column> tables = executor.list("SELECT '" + db + "', col.TABLE_NAME `table`,col.COLUMN_NAME `column`,col.COLUMN_TYPE type,if(IS_NULLABLE='YES',true,false) null_able FROM information_schema.`COLUMNS` col  WHERE TABLE_SCHEMA='" + db + "'", Column.class);
            tableList.addAll(tables);
            semiCache.setValue(CoreCacheKey.COLUMNS.getKey() + dataSource, tableList);
        });
    }

}
