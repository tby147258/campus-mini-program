package com.campus.common;

import lombok.Getter;

/**
 * 统一响应封装
 *
 * code: 业务状态码（200=成功，400=参数错误，401=未登录，403=无权限，500=服务器错误）
 * msg:  响应消息
 * data: 响应数据
 */
@Getter
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    private Result() {}

    // ========== 成功响应 ==========

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    // ========== 失败响应 ==========

    public static <T> Result<T> error(String msg) {
        return error(400, msg);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}