package com.semifinished.core.utils;


import com.semifinished.core.exception.CodeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Map相关工具类
 */
public class MapUtils {
    /**
     * 用于创建Map<String, Object>格式的map
     *
     * @param items 格式：key,value,key,value...
     * @return 创建的map
     */
    public static <K, V> Map<K, V> of(Object... items) {
        if (items.length == 0 || (items.length & 1) != 0) {
            throw new CodeException("MapUtil.of参数错误");
        }
        Map<K, V> map = new HashMap<>();
        K key = null;
        int i = 1;
        for (Object item : items) {
            if ((i++ & 1) == 1) {
                key = (K) item;
                map.put(key, null);
            } else {
                map.put(key, (V) item);
            }
        }
        return map;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }


}
