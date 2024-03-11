package com.semifinished.core.jdbc.parser.query.keyvalueparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.jdbc.SqlDefinition;
import org.springframework.core.Ordered;

//todo 指定id查询树
//todo 计数查询
//todo user表名设置成可配置
//todo 校验会导致错误sql的请求规则
//todo 参数校验，数据库配置，添加参数校验规则接口，使用
//todo 无论增删查改都用sqlDefinition实现，使用sqlCombiner判断type实现不同sql拼接，具体的sql拼接在sqlCreator
//todo 删除全部请求必须携带一个semiId，且这个semiId和当前账户具有删除全部的权限？
// todo 添加日志


/**
 * 前端参数的解析器接口
 */
public interface KeyValueParamsParser extends Ordered {
    /**
     * 对前端传过来的json参数进行解析
     * 具体示例可查看实现类，如{@link LikeParamsParser}
     *
     * @param table         表名，在调用之前对table进行过校验，可以保证能从semicache中获取字段列表
     * @param key           前端json参数key
     * @param value         前端json参数value
     * @param sqlDefinition SQL定义信息
     * @return 是否使用了该解析器，如果为true,则该查询条件不再进行下一次解析
     */
    boolean parse(String table, String key, JsonNode value, SqlDefinition sqlDefinition);
}
