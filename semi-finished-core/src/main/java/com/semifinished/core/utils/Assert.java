package com.semifinished.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.semifinished.core.exception.ProjectRuntimeException;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * 判断工具类，如果方法名的描述不成立，那么抛出异常
 * 如isFalse，那么就是传入的参数是true的情况下，抛出异常
 * hasNotText，传入参数在有text的情况下，抛出异常
 */
public class Assert {

    public static void notNull(Object o, Supplier<ProjectRuntimeException> supplier) {
        if (o == null) {
            throw supplier.get();
        }
    }

    public static void isNull(Object o, Supplier<ProjectRuntimeException> supplier) {
        if (o != null) {
            throw supplier.get();
        }
    }

    public static void isFalse(boolean b, Supplier<ProjectRuntimeException> supplier) {
        if (b) {
            throw supplier.get();
        }
    }

    public static void isTrue(boolean b, Supplier<ProjectRuntimeException> supplier) {
        if (!b) {
            throw supplier.get();
        }
    }

    public static void notBlank(String str, Supplier<ProjectRuntimeException> supplier) {
        if (!StringUtils.hasText(str)) {
            throw supplier.get();
        }
    }

    public static void isBlank(String str, Supplier<ProjectRuntimeException> supplier) {
        if (StringUtils.hasText(str)) {
            throw supplier.get();
        }
    }


    public static void notEmpty(ContainerNode<?> jsonNode, Supplier<ProjectRuntimeException> supplier) {
        if (jsonNode == null || jsonNode.isEmpty()) {
            throw supplier.get();
        }
    }

    public static void notEmpty(Collection<?> collection, Supplier<ProjectRuntimeException> supplier) {
        if (collection == null || collection.isEmpty()) {
            throw supplier.get();
        }
    }

    public static void notEmpty(Object[] array, Supplier<ProjectRuntimeException> supplier) {
        if (array == null || array.length == 0) {
            throw supplier.get();
        }
    }

    public static void notMissNode(JsonNode jsonNode, Supplier<ProjectRuntimeException> supplier) {
        if (jsonNode == null || jsonNode.isMissingNode()) {
            throw supplier.get();
        }
    }
}
