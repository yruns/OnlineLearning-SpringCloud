package com.yruns.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @description 通用结果类型
 * @author Mr.M
 * @date 2022/9/13 14:44
 * @version 1.0
 */

@Data
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应编码,0为正常,-1错误
     */
    private int code;

    /**
     * 响应提示信息
     */
    private String msg;

    /**
     * 响应内容
     */
    private T result;


    public Result() {
        this(0, "success");
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 错误信息的封装
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> response = new Result<T>();
        response.setCode(-1);
        response.setMsg(msg);
        return response;
    }


    public static <T> Result<T> fail(T result, String msg) {
        Result<T> response = new Result<T>();
        response.setCode(-1);
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }



    /**
     * 添加正常响应数据（包含响应内容）
     *
     * @return Result Rest服务封装相应数据
     */
    public static <T> Result<T> ok(T result) {
        Result<T> response = new Result<T>();
        response.setResult(result);
        return response;
    }
    public static <T> Result<T> ok(T result, String msg) {
        Result<T> response = new Result<T>();
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }

    /**
     * 添加正常响应数据（不包含响应内容）
     *
     * @return Result Rest服务封装相应数据
     */
    public static <T> Result<T> ok() {
        return new Result<T>();
    }


    public Boolean isSuccessful() {
        return this.code == 0;
    }

}