package com.semifinished.core.jdbc.parser;

import com.semifinished.core.jdbc.parser.query.keyvalueparser.KeyValueParamsParser;
import org.springframework.core.annotation.Order;
//todo 同一个事务的请求
//todo 表字典查询可以深度查询
//todo 指定id查询树
//todo 计数查询
//todo user表名设置成可配置
//todo 校验会导致错误sql的请求规则
//todo 参数校验，数据库配置，添加参数校验规则接口，使用
//todo 无论增删查改都用sqlDefinition实现，使用sqlCombiner判断type实现不同sql拼接，具体的sql拼接在sqlCreator
//todo 删除全部请求必须携带一个semiId，且这个semiId和当前账户具有删除全部的权限？

/**
 * 解析器接口
 * todo 添加日志
 */
@Order(0)
public interface SelectParamsParser extends KeyValueParamsParser {

}
