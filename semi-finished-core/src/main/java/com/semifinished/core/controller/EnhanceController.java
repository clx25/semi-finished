package com.semifinished.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.annontation.RequestApi;
import com.semifinished.core.annontation.RequestParamNode;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.service.QueryCommonService;
import com.semifinished.core.service.QueryService;
import com.semifinished.core.service.UpdateCommonService;
import com.semifinished.core.service.UpdateService;
import com.semifinished.core.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class EnhanceController {

    private final UpdateCommonService updateCommonService;
    private final QueryCommonService queryCommonService;

    private final List<UpdateService> updateServices;
    private final List<QueryService> queryServices;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 根据条件获取所有数据
     *
     * @param params 查询条件
     * @return 查询出的结果列表
     */
    @PostMapping(value = "enhance", name = "SEMI-JSON-API-POST-QUERY")
    public Object queryPostMapping(@RequestBody(required = false) ObjectNode params) {
        return Result.success(chooseService(queryServices, queryCommonService).commonQuery(params));
    }

    /**
     * 配置接口GET请求的调用方法
     *
     * @param params 请求参数
     * @return 结果列表
     */
    @GetMapping(value = "enhance", name = "SEMI-JSON-API-GET")
    public Object queryGetMapping(@RequestParamNode(required = false) ObjectNode params) {
        return Result.success(chooseService(queryServices, queryCommonService).commonQuery(params));
    }


    /**
     * 新增数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
    @PostMapping(value = "common", name = "SEMI-JSON-API-POST")
    public Result add(@RequestBody JsonNode params) {
        String id = chooseService(updateServices, updateCommonService).add(params);
        return Result.success(id);
    }

    /**
     * 修改数据
     *
     * @param params 请求参数
     * @return 执行结果
     */
    @PutMapping(value = "common", name = "SEMI-JSON-API-PUT")
    public Result update(@RequestBody JsonNode params) {
        chooseService(updateServices, updateCommonService).update(params);
        return Result.success();
    }

    /**
     * 根据条件删除数据
     *
     * @param params 请求参数
     * @return 操作结果
     */
    @DeleteMapping(value = "enhance", name = "SEMI-JSON-API-DELETE")
    public Result delete(@RequestParamNode(required = false) ObjectNode params) {
        chooseService(updateServices, updateCommonService).delete(params);
        return Result.success();
    }

    /**
     * 选择调用的service对象
     *
     * @param services       service对象集合
     * @param defaultService 当没有符合条件的service对象，则返回这个默认的对象
     * @return 删选出的或者默认的service对象
     */
    private <T> T chooseService(List<T> services, T defaultService) {
        HttpServletRequest request = RequestUtils.getRequest();
        String servletPath = request.getServletPath();
        String method = request.getMethod();

        for (T service : services) {
            RequestApi api = service.getClass().getAnnotation(RequestApi.class);
            if (api == null) {
                continue;
            }
            String path = api.path();
            if (!StringUtils.hasText(path)) {
                continue;
            }
            if (method.equalsIgnoreCase(api.method()) && pathMatcher.match(path, servletPath)) {
                return service;
            }
        }
        return defaultService;
    }
}
