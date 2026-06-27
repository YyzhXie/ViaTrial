package com.viatrial.common;

public enum ErrorCode {

    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "数据不存在"),
    CONFLICT(409, "数据冲突"),
    SYSTEM_ERROR(500, "系统内部错误");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
