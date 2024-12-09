package com.semifinished.core.jdbc.executor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.*;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.ValueCondition;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;


@Component
@AllArgsConstructor
public class MySqlExecutor implements Executor {
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final TableUtils tableUtils;
    private final List<AfterUpdateEnhance> afterUpdateEnhances;

    @Override
    public String dialect() {
        return "mysql";
    }

    @Override
    public List<ObjectNode> query(SqlDefinition sqlDefinition) {
        //组装查询SQL并获取
        String sql = QuerySqlCombiner.query(sqlDefinition);

        //执行查询
        return sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .list(sql, QuerySqlCombiner.getArgs(sqlDefinition));

    }


    @Override
    public void add(SqlDefinition sqlDefinition, String idKey) {
        transactional(sqlDefinition, executor -> {
            String sql = UpdateSqlCombiner.addSQLExcludeId(sqlDefinition, configProperties.getIdKey());
            String id = executor.insert(sql, QuerySqlCombiner.getArgs(sqlDefinition));
            List<Column> columns = sqlDefinition.getColumns();
            if (columns == null) {
                return;
            }
            for (Column column : columns) {
                if (column.getColumn().equals(idKey)) {
                    sqlDefinition.setId(id);
                    return;
                }
            }

        });

    }

    @Override
    public void update(SqlDefinition sqlDefinition) {
        transactional(sqlDefinition, executor -> {
            String sql = UpdateSqlCombiner.updateByIdSQL(sqlDefinition, configProperties.getIdKey());
            executor.update(sql, UpdateSqlCombiner.getUpdateArgs(sqlDefinition));
        });
    }


    @Override
    public void delete(SqlDefinition sqlDefinition) {
        transactional(sqlDefinition, executor -> {
            String idKey = configProperties.getIdKey();
            String sql;
            if (configProperties.isLogicDelete()) {
                String logicDeleteColumn = configProperties.getLogicDeleteColumn();
                tableUtils.validColumnsName(sqlDefinition, sqlDefinition.getTable(), logicDeleteColumn);
                sql = UpdateSqlCombiner.logicDeleteSQL(sqlDefinition, idKey, logicDeleteColumn, configProperties.isFreeDelete());
            } else {
                sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, idKey, configProperties.isFreeDelete());
            }

            executor.update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });

    }

    /**
     * 通过事务执行语句
     *
     * @param sqlDefinition SQL定义信息
     * @param consumer      不同调用者执行的内容，该内容会被手动或自动执行
     */
    private void transactional(SqlDefinition sqlDefinition, Consumer<SqlExecutor> consumer) {
        sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .transaction(executor -> {

                    List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
                    Assert.isEmpty(valueConditions, () -> new ParamsException("参数不能为空"));

                    //过滤掉
                    long count = valueConditions.stream().filter(v -> v.getCondition().startsWith("=:") || v.getCondition().trim().equals("is null")).count();
                    Assert.isFalse(count > 0, () -> new ParamsException("参数不能为空"));

                    Assert.hasNotText(sqlDefinition.getTable(), () -> new ParamsException("未指定表名"));

                    AfterUpdateEnhance.SqlAutoExecutor sqlAutoExecutor = new AfterUpdateEnhance.SqlAutoExecutor(executor, consumer);

                    for (AfterUpdateEnhance enhance : afterUpdateEnhances) {
                        enhance.transactional(sqlAutoExecutor, sqlDefinition);
                    }
                    sqlAutoExecutor.exec();
                });

    }
}
