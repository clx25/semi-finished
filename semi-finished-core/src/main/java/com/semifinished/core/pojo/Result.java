package com.semifinished.core.pojo;


import com.semifinished.core.constant.ResultInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 用于包装返回数据
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    /**
     * 消息
     */
    private String msg;
    /**
     * 状态码
     */
    private int code;
    /**
     * 返回数据
     */
    private Object result;

    public static Result success() {
        return info(ResultInfo.SUCCESS);
    }

    public static Result success(Object o) {
        Result result = success();
        result.setResult(o);
        return result;
    }

    public static Result info(ResultInfo resultInfo) {
        Result result = new Result();
        result.setCode(resultInfo.getCode());
        result.setMsg(resultInfo.getMsg());
        return result;
    }

    public static Result error(int code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
