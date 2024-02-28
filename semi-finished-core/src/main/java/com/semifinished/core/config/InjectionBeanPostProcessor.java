package com.semifinished.core.config;


import com.semifinished.core.exception.CodeException;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.annontation.Semi;
import org.apache.commons.math3.util.Pair;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 给@Semi注解的字段找到合适的bean，并赋值
 *
 * @see Semi
 */
@Order
@Component
public class InjectionBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {


    /**
     * 判断类是否在指定的包中，多个包名集合，只要符合其中一个就返回true
     *
     * @param packages 包名集合
     * @param aClass   需要判断的类
     * @return true表示在指定的包中，false表示不在
     */
    private static boolean isInPackages(List<String> packages, Class<?> aClass) {
        return packages.stream().anyMatch(pkg -> aClass.getName().startsWith(pkg + "."));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        //获取项目所在包名
        List<String> packages = AutoConfigurationPackages.get(applicationContext.getAutowireCapableBeanFactory());
        //把com.semifinished包添加到扫描包
        if (!packages.contains("com.semifinished")) {
            packages.add("com.semifinished");
        }


        Map<Class<?>, Map<String, Object>> annoBeanMap = new HashMap<>();

        MultiValueMap<Class<?>, Pair<Object, Field>> annoFieldMap = new LinkedMultiValueMap<>();

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            //扫描所有bean，判断bean是否在指定的包中
            if (!isInPackages(packages, bean.getClass())) {
                continue;
            }

            //继续筛选semi注解的bean
            filterSemiBean(annoBeanMap, beanName, bean);

            //筛选semi注解的字段
            filterSemiField(annoFieldMap, bean);
        }

        injection(annoBeanMap, annoFieldMap);
    }

    /**
     * 筛选出有@Semi注解的字段并保存到map中
     *
     * @param annoFieldMap 有@Semi注解的字段
     * @param bean         需要判断的字段所属的bean
     */
    private void filterSemiField(MultiValueMap<Class<?>, Pair<Object, Field>> annoFieldMap, Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            Arrays.stream(field.getAnnotations()).filter(this::isMetaAnnotationsBySemi)
                    .forEach(anno -> annoFieldMap.add(anno.annotationType(), Pair.create(bean, field)));
        }
    }

    /**
     * 筛选出有@Semi注解的bean并保存到map中
     *
     * @param annoBeanMap 有@Semi注解的bean
     * @param beanName    bean的名称
     * @param bean        需要判断的字段所属的bean
     */
    private void filterSemiBean(Map<Class<?>, Map<String, Object>> annoBeanMap, String beanName, Object bean) {
        Arrays.stream(bean.getClass().getAnnotations())
                .filter(this::isMetaAnnotationsBySemi)
                .forEach(anno -> annoBeanMap.computeIfAbsent(anno.annotationType(), (value) -> new HashMap<>()).put(beanName, bean));
    }

    /**
     * 给对应字段注入bean
     *
     * @param annoBeanMap  有@Semi注解的bean
     * @param annoFieldMap 有@Semi注解的字段
     */
    private void injection(Map<Class<?>, Map<String, Object>> annoBeanMap, MultiValueMap<Class<?>, Pair<Object, Field>> annoFieldMap) {
        for (Map.Entry<Class<?>, List<Pair<Object, Field>>> entry : annoFieldMap.entrySet()) {
            Map<String, Object> beans = annoBeanMap.get(entry.getKey());
            if (CollectionUtils.isEmpty(beans)) {
                continue;
            }
            injectionAssignableType(entry.getValue(), beans);
        }
    }

    /**
     * 筛选出能适配字段类型的bean
     *
     * @param fields 包含字段与所属bean
     * @param beans  包含字段注解对应的所有bean
     */
    private void injectionAssignableType(List<Pair<Object, Field>> fields, Map<String, Object> beans) {
        for (Pair<Object, Field> pair : fields) {
            pair.getValue().setAccessible(true);
            Map<String, Object> beanIn = new HashMap<>();
            beans.forEach((k, v) -> {
                if (getFieldType(pair.getValue()).isAssignableFrom(v.getClass())) {
                    beanIn.put(k, v);
                }
            });
            setValue(pair.getValue(), pair.getKey(), beanIn);
        }
    }

    /**
     * 给字段赋值
     *
     * @param field 赋值字段
     * @param bean  赋值字段所属的bean
     * @param beans 赋给字段的值
     */
    private void setValue(Field field, Object bean, Map<String, Object> beans) {
        if (List.class.isAssignableFrom(field.getType())) {
            List<Object> beansList = beans.values().stream().sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
            set(field, bean, beansList);
        } else if (Set.class.isAssignableFrom(field.getType())) {
            Set<Object> beansSet = new HashSet<>(beans.values());
            set(field, bean, beansSet);
        } else if (Map.class.isAssignableFrom(field.getType())) {
            set(field, bean, beans);
        } else {
            String name = field.getName();
            Object o = beans.get(name);
            if (o != null) {
                set(field, bean, o);
            }
        }
    }

    /**
     * 判断Semi是不是该注解的直接或间接元注解
     *
     * @param annotation 需要判断的注解
     * @return true表示该注解直接或间接有semi作为元注解，false表示没有
     */
    private boolean isMetaAnnotationsBySemi(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (annotationType.isAssignableFrom(Semi.class)) {
            return true;
        }
        Annotation[] annotations = annotationType.getAnnotations();
        for (Annotation annotationByAnnotation : annotations) {
            if (annotationByAnnotation.annotationType() == annotationType) {
                return false;
            }
            if (isMetaAnnotationsBySemi(annotationByAnnotation)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取字段类型
     *
     * @param field 字段
     * @return 字段类型
     */
    private Class<?> getFieldType(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            Class<?> componentType = fieldType.getComponentType();
            return ResolvableType.forField(field, 1, null).resolve(componentType);
        } else if (Collection.class.isAssignableFrom(fieldType) && fieldType.isInterface()) {
            return ResolvableType.forField(field, 1, null).asCollection().resolveGeneric(0);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            ResolvableType resolvableType = ResolvableType.forField(field, 1, null).asMap();
            Class<?> aClass = resolvableType.resolveGeneric(0);
            Assert.isFalse(aClass == String.class, () -> new CodeException("@Semi注入的Map，key必须为String类型 " + field));
            return resolvableType.resolveGeneric(1);
        } else {
            return fieldType;
        }
    }


    private void set(Field field, Object bean, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new CodeException(field + "字段注入错误");
        }
    }

}
