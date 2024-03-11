package com.semifinished.core.service.enhance;


import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.parser.query.ParamsParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 对查询结果进行增强
 * <p>
 * {@link ParamsParser#parse}可以通过order的大小实现{@link #beforeParse}与{@link #afterParse}的功能替代。
 * 但这两个类的使用方向不同，{@link ParamsParser#parse}用于对请求参数的解析，而{@link ServiceEnhance}中的方法用于对结果进行处理。
 * 当增强需要请求参数具有某些条件才能实现时，那么{@link #beforeParse}与{@link #afterParse}可以提前对请求参数进行处理，以满足要求，而不需要把增强类中的功能拆分到解析类中
 */
public interface ServiceEnhance {


    /**
     * 判断请求是否使用该增强,默认使用
     *
     * @param sqlDefinition SQL定义信息
     * @return true使用增强 ，false不使用增强
     */
    default boolean support(SqlDefinition sqlDefinition) {
        return true;
    }

    /**
     * 判断请求参数中@bean的值是否等于实现类的beanName
     *
     * @param sqlDefinition SQL定义信息
     * @return 如果请求参数中@bean的值等于实现类的beanName那么返回true，不想等则返回false
     */
    default boolean supportForBeanName(SqlDefinition sqlDefinition) {
        String beans = sqlDefinition.getParams().path("@bean").asText(null);
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
     * 在参数解析参数之前执行
     *
     * @param sqlDefinition SQL定义信息
     */
    default void beforeParse(SqlDefinition sqlDefinition) {

    }


    /**
     * 参数解析完成后执行，可以直接对解析完成后的内容进行修改，并直接影响最终的sql
     *
     * @param sqlDefinition SQL定义信息
     */
    default void afterParse(SqlDefinition sqlDefinition) {

    }


}
