package com.semifinished.excel.service;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.exception.ConfigException;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.RequestUtils;
import com.semifinished.excel.handler.ExcelHandler;
import com.semifinished.excel.listener.ExcelListener;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExcelService {
    private final SemiCache semiCache;
    private final ObjectMapper objectMapper;
    private final List<ExcelHandler> excelHandlers;


    /**
     * 解析excel并调用处理器
     *
     * @param file   excel文件
     * @param params 请求参数
     */
    public void parseExcel(MultipartFile file, ObjectNode params) {

        Assert.isTrue(this.excelHandlers == null, () -> new CodeException("未配置excel处理类"));

        HttpServletRequest request = RequestUtils.getRequest();
        String servletPath = request.getServletPath();

        //获取此次excel请求的参数
        Map<String, ObjectNode> configs = semiCache.getHashValue(CoreCacheKey.JSON_CONFIGS.getKey(), "POST");
        Assert.isTrue(configs == null, () -> new ConfigException("未配置excel解析"));

        ObjectNode currentConfigs = configs.get(servletPath);
        Assert.isTrue(currentConfigs == null, () -> new ConfigException("未配置该请求excel解析"));

        //获取表头的配置信息
        JsonNode headerNode = currentConfigs.path("header");
        Assert.isTrue(headerNode == null, () -> new ConfigException("未配置该请求表头"));
        Map<String, String> headerConfig = objectMapper.convertValue(headerNode, new TypeReference<Map<String, String>>() {
        });

        //保存excel解析后的数据
        ArrayNode rows = JsonNodeFactory.instance.arrayNode();
        //保存解析后的表头
        Map<Integer, String> header = new HashMap<>();

        try {
            EasyExcel.read(file.getInputStream(), new ExcelListener(rows, headerConfig, header)).sheet().doRead();
        } catch (IOException e) {
            throw new ParamsException("excel文件异常");
        }

        //筛选匹配的ExcelHandler
        List<ExcelHandler> matchHandlers = this.excelHandlers.stream()
                .filter(handler -> handler.path() != null)
                .filter(handler -> Arrays.stream(handler.path())
                        .map(path -> {
                            if (!path.startsWith("/")) {
                                path = "/" + path;
                            }
                            return path;
                        }).anyMatch(servletPath::equals))
                .collect(Collectors.toList());

        Assert.isTrue(matchHandlers.isEmpty(), () -> new CodeException("没有对应处理器"));
        matchHandlers.forEach(handler -> handler.handle(currentConfigs, rows, header));
    }


}
