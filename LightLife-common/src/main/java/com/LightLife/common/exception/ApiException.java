package com.LightLife.common.exception;

public class ApiException extends Exception {

    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return getMessage();
    }
}