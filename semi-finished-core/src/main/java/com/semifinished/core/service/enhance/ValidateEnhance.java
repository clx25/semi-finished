package com.semifinished.core.service.enhance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

        ObjectNode apiConfigs = apiMap.get(servletPath);
        if (apiConfigs == null) {
            return;
        }
        JsonNode ruler = apiConfigs.get("ruler");
        if (ruler == null) {
            return;
        }

        ruler.fields().forEachRemaining(entry -> validate(entry.getKey(), entry.getValue(), sqlDefinition.getRawParams(), sqlDefinition));
    }


    /**
     * 参数校验
     *
     * @param field         规则字段
     * @param value         规则值
     * @param params        请求参数
     * @param sqlDefinition SQL定义信息
     */
    private void validate(String field, JsonNode value, JsonNode params, SqlDefinition sqlDefinition) {

        if (params instanceof ArrayNode) {
            for (JsonNode param : params) {
                validate(field, value, param, sqlDefinition);
            }
            return;
        }

        JsonNode jsonNode = params.get(field);
        String text = jsonNode == null ? null : jsonNode.asText(null);

        value.fields().forEachRemaining(entry -> {
            for (Validator validator : validators) {
                String pattern = entry.getKey().trim();
                String msg = entry.getValue().asText("");
                boolean validate = validator.validate(field, text, pattern, msg, sqlDefinition);
                if (validate) {
                    return;
                }
            }
        });


    }
}
