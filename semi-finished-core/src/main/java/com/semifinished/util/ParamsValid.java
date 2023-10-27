package com.semifinished.util;


import com.semifinished.exception.ParamsException;

import java.util.Map;

/**
 * 对传入的参数进行校验
 */
public class ParamsValid {


    public static void valid(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            Assert.hasNotText(key, () -> new ParamsException("字段名不能为空"));
            Assert.isFalse(ParamsUtils.isLegalName(key), () -> new ParamsException("字段名" + entry.getKey() + "错误"));
        }
    }

    public static void valid(String str, String msg) {
        Assert.isFalse(ParamsUtils.isLegalName(str), () -> new ParamsException(msg));
    }


    public static void validAndStr(String... names) {
        for (String name : names) {
            Assert.hasNotText(name, () -> new ParamsException("参数错误"));
        }
    }
}
