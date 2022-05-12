package com.fanqie.common.exception;

/**
 * 自定义异常
 *
 */
public class ShiroException extends Exception {

    private Integer code;

    private String message;

    public ShiroException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
