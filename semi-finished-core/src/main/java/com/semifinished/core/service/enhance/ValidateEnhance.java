package com.semifinished.core.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.cache.CoreCacheKey;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.service.enhance.update.AfterUpdateEnhance;
import com.semifinished.core.service.enhance.validator.Validator;
import com.semifinished.core.utils.RequestUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Component
@Order(-800)
@AllArgsConstructor
public class ValidateEnhance implements AfterQueryEnhance, AfterUpdateEnhance {

    private final SemiCache semiCache;
    private final List<Validator> validators;

    @Override
    public void afterParse(SqlDefinition sqlDefinition) {
        HttpServletRequest request = RequestUtils.getRequest();
        String servletPath = request.getServletPath();
        String method = request.getMethod();

        Map<String, ObjectNode> apiMap = semiCache.getValue(CoreCacheKey.CUSTOM_API.getKey(), method);
        if (apiMap == null) {
            return;
        }

        ObjectNode objectNode = apiMap.get(servletPath);
        if (objectNode == null) {
            return;
        }
        validate(sqlDefinition.getRawParams(), objectNode, sqlDefinition);
    }


    /**
     * 参数校验
     *
     * @param params        请求参数
     * @param apiConfigs    配置信息
     * @param sqlDefinition SQL定义信息
     */
    private void validate(JsonNode params, ObjectNode apiConfigs, SqlDefinition sqlDefinition) {

        JsonNode ruler = apiConfigs.get("ruler");
        if (ruler == null) {
            return;
        }

        ruler.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            String text = params.get(field).asText(null);

            entry.getValue().fields().forEachRemaining(patternEntry -> {
                for (Validator validator : validators) {
                    String pattern = patternEntry.getKey().trim();
                    String msg = patternEntry.getValue().asText("");
                    boolean validate = validator.validate(field, text, pattern, msg, sqlDefinition);
                    if (validate) {
                        return;
                    }
                }
            });

        });


    }
}
