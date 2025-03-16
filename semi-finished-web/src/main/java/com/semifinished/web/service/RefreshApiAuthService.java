package com.semifinished.web.service;

import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.core.annontation.RequestApi;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.service.UpdateAbstractService;
import com.semifinished.core.service.UpdateService;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.web.pojo.ApiAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 启用/关闭接口权限
 */
@Service
@RequiredArgsConstructor
@RequestApi(path = "/refresh/apiAuth", method = "put")
public class RefreshApiAuthService extends UpdateAbstractService implements UpdateService {

    private final SemiCache semiCache;
    private final SqlExecutorHolder sqlExecutorHolder;

    @Override
    public void afterExecute(SqlDefinition sqlDefinition, List<AfterUpdateEnhance> afterUpdateEnhances) {
        super.afterExecute(sqlDefinition, afterUpdateEnhances);

        String id = sqlDefinition.getId();

        ApiAuth apiAuth = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource()).getJdbcTemplate()
                .queryForObject("select * from semi_api_auth where id = ?"
                        , new BeanPropertyRowMapper<>(ApiAuth.class)
                        , id
                );
        if (apiAuth == null) {
            return;
        }
        Map<String, String> skipAuth = semiCache.getValue(AuthCacheKey.SKIP_AUTH.getKey());
        if (apiAuth.isDisabled()) {
            skipAuth.remove(apiAuth.getPattern());
            return;
        }
        skipAuth.put(apiAuth.getPattern(), apiAuth.getMethod());
    }

}

