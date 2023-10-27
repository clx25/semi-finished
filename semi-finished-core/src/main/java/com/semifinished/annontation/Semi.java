package com.semifinished.annontation;

import java.lang.annotation.*;

/**
 * 标记一个自定义注解注解
 * 用于根据注解注入对于的bean
 * 使用方法：
 * <pre>
 * {@code
 *     \@Semi
 *     \@Target({ElementType.TYPE, ElementType.FIELD})
 *     \@Retention(RetentionPolicy.RUNTIME)
 *     public @interface Abc{
 *      //创建一个注解，使用@Semi作为元注解
 *     }
 *
 *     \@Abc
 *     public class A implements D {
 *
 *     }
 *
 *     \@Abc
 *     public class B implements D {
 *
 *     }
 *
 *     \@Component
 *     public class C{
 *
 *         \@Abc
 *         private A a;
 *
 *         \@Abc
 *         private List<D> dList;
 *
 *         \@Abc
 *         private Set<D> dSet;
 *
 *         \@Abc
 *         private Map<String,D> dMap;
 *      }
 * }
 * </pre>
 * 如以上代码所示，只要一个注解以@Semi为直接或者间接元注解
 * 把该注解标注在类上，再把这个注解标注在bean的字段上，就能注入对应类型并且相同注解的bean，如果没有，就不注入
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Semi {
}
