package com.semifinished.core.constant;

import lombok.Getter;

/**
 * 返回数据的常用消息和状态码
 */
@Getter
public enum ResultInfo {
    DATA_NOT_FIND("数据未找到", 4001),
    PAYLOAD_ERROR("请求参数错误", 4005),
    METHOD_ERROR("请求方式错误", 4006),
    PAYLOAD_NOT_EMPTY("参数不能为空", 4007),
    UPLOAD_ERROR("文件上传错误", 4008),
    SQL_ERROR("参数错误，请检查参数", 5001),
    LOGIN_SUCCESS("登录成功", 200),
    SUCCESS("操作成功", 200);

    private final String msg;
    private final int code;

    ResultInfo(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }
}