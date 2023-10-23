package com.example.exception;

public class SystemException extends RuntimeException{
    private Integer Code;

    public SystemException(Integer Code, String message) {
        super(message);
        this.Code = Code;
    }

    public SystemException(Integer Code,String message, Throwable cause) {
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
