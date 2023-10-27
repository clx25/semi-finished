package com.semifinished.service.enhance;


import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.parser.query.ParamsParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 对查询结果进行增强
 * <p>
 * {@link ParamsParser#parser}可以通过order的大小实现{@link #beforeParse}与{@link #afterParse}的功能替代。
 * 但这两个类的使用方向不同，{@link ParamsParser#parser}用于对请求参数的解析，而{@link ServiceEnhance}中的方法用于对结果进行处理。
 * 当增强需要请求参数具有某些条件才能实现时，那么{@link #beforeParse}与{@link #afterParse}可以提前对请求参数进行处理，以满足要求，而不需要把增强类中的功能拆分到解析类中
 */
public interface ServiceEnhance {

    /**
     * 默认增强类的名称，
     * 按优先级从大到小
     * 1. 重写该方法后返回的值
     * 2. @Component注解的value值
     * 3. 首字母小写的类名
     */
    default String name() {
        Component component = this.getClass().getAnnotation(Component.class);
        if (component != null) {
            String value = component.value();
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return StringUtils.uncapitalize(this.getClass().getSimpleName());
    }


    /**
     * 判断请求是否使用该增强
     * 如果没有@bean参数，默认返回true
     * 请求中的@bean参数为使用的增强名称字符串，多个用逗号分隔
     * 在默认规则中，如果参数中的增强名称字符串与{@link #name()}返回的名称相匹配，表示要使用该增强
     *
     * @param sqlDefinition sql相关的数据
     * @return true使用增强 ，false不使用增强
     */
    default boolean support(SqlDefinition sqlDefinition) {
        JsonNode path = sqlDefinition.getParams().path("@bean");
        if (path.isMissingNode()) {
            return true;
        }
        String beans = path.asText(null);
        if (!StringUtils.hasText(beans)) {
            return false;
        }
        String name = name();
        for (String bean : beans.split(",")) {
            if (name != null && Objects.equals(name, bean)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 在解析参数之前执行
     *
     * @param sqlDefinition sql相关的数据x
     */
    default void beforeParse(SqlDefinition sqlDefinition) {

    }


    /**
     * 参数解析完成后执行，可以直接对解析完成后的内容进行修改，并直接影响最终的sql
     *
     * @param sqlDefinition 解析完成后的sqlDefinition
     */
    default void afterParse(SqlDefinition sqlDefinition) {

    }


}
