package com.semifinished.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.config.ConfigProperties;
import com.semifinished.exception.ParamsException;
import com.semifinished.jdbc.QuerySqlCombiner;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.SqlExecutor;
import com.semifinished.jdbc.UpdateSqlCombiner;
import com.semifinished.jdbc.parser.query.CommonParser;
import com.semifinished.jdbc.parser.query.ParamsParser;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.service.enhance.AfterUpdateEnhance;
import com.semifinished.util.Assert;
import com.semifinished.util.bean.TableUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class UpdateService {

    private final TableUtils tableUtils;
    private final CommonParser commonParser;
    private final List<ParamsParser> paramsParsers;
    private final List<AfterUpdateEnhance> afterUpdateEnhances;
    private final SqlExecutor sqlExecutor;
    private final ConfigProperties configProperties;

    public void update(ObjectNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.updateSQL(sqlDefinition, configProperties.getIdKey());
            sqlExecutor.update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });
    }

    public void delete(String id) {
        ObjectNode params = JsonNodeFactory.instance.objectNode().put(configProperties.getIdKey(), id);
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.deleteSQL(sqlDefinition, configProperties.getIdKey());
            sqlExecutor.update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });
    }

    public void add(ObjectNode params) {
        execute(params, (sqlDefinition) -> {
            String sql = UpdateSqlCombiner.addSQLExcludeId(sqlDefinition, configProperties.getIdKey());
            sqlExecutor.update(sql, QuerySqlCombiner.getArgs(sqlDefinition));
        });

    }

    private void execute(ObjectNode params, Consumer<SqlDefinition> consumer) {
        Assert.isEmpty(params, () -> new ParamsException("参数不能为空"));

        SqlDefinition sqlDefinition = new SqlDefinition(params);

        afterUpdateEnhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));

        paramsParsers.forEach(parser -> parser.parse(params, sqlDefinition));

        afterUpdateEnhances.forEach(enhance -> enhance.afterParse(sqlDefinition));


        sqlExecutor.transaction(executor -> {

            List<ValueCondition> valueConditions = sqlDefinition.getValueCondition();
            Assert.isEmpty(valueConditions, () -> new ParamsException("参数不能为空"));
            Assert.hasNotText(sqlDefinition.getTable(), () -> new ParamsException("未指定表名"));

            AfterUpdateEnhance.SqlAutoExecutor sqlAutoExecutor = new AfterUpdateEnhance.SqlAutoExecutor(consumer, sqlDefinition);


            for (AfterUpdateEnhance enhance : afterUpdateEnhances) {
                enhance.transactional(sqlAutoExecutor,sqlDefinition);
            }
            sqlAutoExecutor.exec();

        });

        afterUpdateEnhances.forEach(enhance -> enhance.afterExecute(sqlDefinition));
    }


}
