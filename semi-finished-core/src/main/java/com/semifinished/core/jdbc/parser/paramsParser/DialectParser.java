package com.semifinished.core.jdbc.parser.paramsParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.executor.Executor;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 查询方言类型解析,默认使用mysql
 * e.g. 指定mysql查询
 * <pre>
 *     {
 *       "@dialect":"mysql" ，
 *     }
 * </pre>
 * "@dialect"具体的值取决于 {@link Executor#dialect()}的返回内容
 */
@Component
@AllArgsConstructor
public class DialectParser implements ParamsParser {
    private final List<Executor> executorList;

    @Override
    public void parse(ObjectNode params, SqlDefinition sqlDefinition) {
        JsonNode dialectNode = params.remove("@dialect");
        if (dialectNode == null) {
            sqlDefinition.setDialect("mysql");
            return;
        }
        String dialect = dialectNode.asText("");
        Assert.isFalse(StringUtils.hasText(dialect), () -> new ParamsException("@dialect参数不能为空"));

        Assert.isFalse(executorList.stream().anyMatch(q -> dialect.equals(q.dialect())), () -> new ParamsException("指定的dialect不存在:%s", dialect));
        Assert.isTrue(executorList.stream().filter(q -> dialect.equals(q.dialect())).count() > 1, () -> new ParamsException("指定的dialect存在多个:%s", dialect));
    }

    @Override
    public int getOrder() {
        return -1100;
    }
}
