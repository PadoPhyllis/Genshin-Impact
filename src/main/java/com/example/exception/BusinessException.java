package com.example.exception;

public class BusinessException extends RuntimeException{
    private Integer Code;

    public BusinessException(Integer Code, String message) {
        super(message);
        this.Code = Code;
    }

    public BusinessException(Integer Code,String message, Throwable cause) {
        super(message, cause);
        this.Code = Code;
    }

    public Integer getCode() {
        return Code;
    }

    public void setCode(Integer code) {
        Code = code;
    }
}
