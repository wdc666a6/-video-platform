package com.example.videoplatform.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code; // 200成功，400失败
    private String msg; // 提示信息
    private T data; // 返回的数据

    // 显式添加getter/setter方法以确保编译通过
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // 成功的静态方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 成功的静态方法（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败的静态方法
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        return result;
    }
}