package com.example.common;

import com.example.exception.BusinessException;
import com.example.exception.SystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProjectExceptionAdvice {
    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException e){
        return new Result(e.getCode(),null,e.getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException e){
        //记录异常
        //发送信息给开发人员
        System.out.println("发现异常中...");
        return new Result(e.getCode(),null,e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Result doException(Exception e){
        //记录异常
        //发送信息给开发人员
        System.out.println("发现异常中...");
        return new Result(666,null,"系统出现异常,请您稍后再试！");
    }
}
