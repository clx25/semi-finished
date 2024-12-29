package com.semifinished.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.factory.ApiSqlDefinitionFactory;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.service.QueryService;
import com.semifinished.core.service.UpdateService;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.ParamsUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class MultiService {
    private final ApiSqlDefinitionFactory apiSqlDefinitionFactory;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    private final UpdateService updateService;
    private final QueryService queryService;

    public void multi(ObjectNode params) {
        ObjectNode template = apiSqlDefinitionFactory.getTemplate();

        sqlExecutorHolder.dataSource(null).transaction(executor -> {

            List<Pair<Integer, Runnable>> execute = new ArrayList<>();

            template.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                Assert.isFalse(value instanceof ObjectNode, () -> new ParamsException("%参数类型错误", key));


                //获取执行排序
                String[] keys = key.split(":");
                Assert.isFalse(keys.length == 2, () -> new ParamsException("参数%s错误", key));
                String index = keys[1];
                Assert.isFalse(ParamsUtils.isInteger(index), () -> new ParamsException("参数%s错误", key));


                switch (keys[0]) {
                    case "u":
                        execute.add(Pair.create(Integer.parseInt(index), () -> {
                            SqlDefinition definition = apiSqlDefinitionFactory.getSqlDefinition(value, params);
                            updateService.update(definition);
                        }));
                        break;
                    case "c":
                        //todo  把上一个新增返回的id作为下一个新增的参数
                        execute.add(Pair.create(Integer.parseInt(index), () -> {
                            SqlDefinition definition = apiSqlDefinitionFactory.getSqlDefinition(value, params);
                            updateService.add(definition);
                            if (!params.has(configProperties.getIdKey())) {
                                params.put(configProperties.getIdKey(), definition.getId());
                            }
                        }));

                        break;
                    case "d":
                        execute.add(Pair.create(Integer.parseInt(index), () -> {
                            SqlDefinition definition = apiSqlDefinitionFactory.getSqlDefinition(value, params);
                            updateService.delete(definition);
                        }));
                        break;
                    case "r":
                        SqlDefinition definition = apiSqlDefinitionFactory.getSqlDefinition(value, params);
                        queryService.query(definition);
                }
            });

            execute.sort(Comparator.comparing(Pair::getKey));

            execute.forEach(p -> p.getValue().run());
        });
    }
}
