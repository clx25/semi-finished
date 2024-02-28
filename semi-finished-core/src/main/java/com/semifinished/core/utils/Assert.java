package com.semifinished.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.core.exception.ProjectRuntimeException;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 判断工具类，如果方法名的描述成立，那么抛出异常
 * 如isFalse，那么就是传入的参数是false的情况下，抛出异常
 * hasNotText，传入参数是没有text的情况下，抛出异常
 */
public class Assert {

    public static void isNull(Object o, Supplier<ProjectRuntimeException> supplier) {
        if (o == null) {
            throw supplier.get();
        }
    }

    public static void isTrue(boolean b, Supplier<ProjectRuntimeException> supplier) {
        if (b) {
            throw supplier.get();
        }
    }

    public static void isFalse(boolean b, Supplier<ProjectRuntimeException> supplier) {
        if (!b) {
            throw supplier.get();
        }
    }

    public static void hasNotText(String str, Supplier<ProjectRuntimeException> supplier) {
        if (!StringUtils.hasText(str)) {
            throw supplier.get();
        }
    }

    public static void hasText(String str, Supplier<ProjectRuntimeException> supplier) {
        if (StringUtils.hasText(str)) {
            throw supplier.get();
        }
    }

    public static void isEmpty(JsonNode jsonNode, Supplier<ProjectRuntimeException> supplier) {
        if (jsonNode == null || jsonNode.isEmpty()) {
            throw supplier.get();
        }
    }

    public static void isEmpty(Collection<?> collection, Supplier<ProjectRuntimeException> supplier) {
        if (collection == null || collection.isEmpty()) {
            throw supplier.get();
        }
    }

    public static void isEmpty(Object[] array, Supplier<ProjectRuntimeException> supplier) {
        if (array == null || array.length == 0) {
            throw supplier.get();
        }
    }
}
