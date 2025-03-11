package com.semifinished.core.utils;


import com.semifinished.core.exception.ParamsException;

import java.util.Map;

/**
 * 对传入的参数进行校验
 */
public class ParamsValid {


    public static void valid(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            Assert.notBlank(key, () -> new ParamsException("字段名不能为空"));
            Assert.isTrue(ParamsUtils.isLegalName(key), () -> new ParamsException("字段名" + entry.getKey() + "错误"));
        }
    }

    public static void valid(String str, String msg) {
        Assert.isTrue(ParamsUtils.isLegalName(str), () -> new ParamsException(msg));
    }


    public static void validAndStr(String... names) {
        for (String name : names) {
            Assert.notBlank(name, () -> new ParamsException("参数错误"));
        }
    }
}
