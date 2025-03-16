package com.semifinished.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.api.excetion.ApiException;
import com.semifinished.api.listener.JsonApiInit;
import com.semifinished.core.annontation.RequestApi;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.service.UpdateAbstractService;
import com.semifinished.core.service.UpdateService;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.web.pojo.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 启用/关闭接口
 */
@RequestApi(path = "/refresh/api", method = "put")
@Service
@RequiredArgsConstructor
public class RefreshApiService extends UpdateAbstractService implements UpdateService {

    private final JsonApiInit jsonApiInit;
    private final ObjectMapper objectMapper;
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public void afterExecute(SqlDefinition sqlDefinition, List<AfterUpdateEnhance> afterUpdateEnhances) {
        super.afterExecute(sqlDefinition, afterUpdateEnhances);

        String id = sqlDefinition.getId();

        Api api = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource()).getJdbcTemplate()
                .queryForObject("select * from semi_api where id = ?"
                        , new BeanPropertyRowMapper<>(Api.class)
                        , id
                );
        if (api == null) {
            return;
        }
        if (api.isDisabled()) {
            jsonApiInit.removeApi(api.getGroupName(), api.getPattern());
            return;
        }
        String path = api.getParams();
        Assert.notNull(path, () -> new ApiException("接口配置参数错误"));
        ObjectNode params;
        try {
            params = objectMapper.readValue(path, ObjectNode.class);
        } catch (JsonProcessingException e) {
            throw new ApiException("接口配置参数错误", e);
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("summary", api.getSummary());
        node.put("version", api.getVersion());
        // node.put("config", api.getConfig());
        node.set("params", params);

        Map<String, Map<String, ObjectNode>> api2 = MapUtils.of(api.getGroupName(), MapUtils.of(api.getPattern(), node));

        jsonApiInit.refreshApi(api2);
    }

}
