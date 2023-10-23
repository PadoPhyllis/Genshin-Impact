package com.example.config;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.common.Code;
import com.example.module.login.JWTUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProjectInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null) {
            returnJson(response,"请您进行登录！");
            return false;
        }

        try {
            JWTUtils.verify(token); //验证令牌
            return true;
        }catch (SignatureVerificationException e){
            //签名异常
            returnJson(response,"无效签名！");
        }catch (TokenExpiredException e){
            //过期异常
            returnJson(response,"登录信息过期，请重新登录！");
        }catch (AlgorithmMismatchException e){
            //算法不匹配
            returnJson(response,"签名解析失败！");
        }catch (Exception e){
            returnJson(response,"身份信息认证失败！");
        }

        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    //拦截返回值
    private void returnJson(HttpServletResponse response, String message) throws Exception{
        PrintWriter writer = null;
        response.setContentType("application/json;charset=UTF-8");
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", Code.TOKENFAIL);
            result.put("data", null);
            result.put("msg", message);

            writer = response.getWriter();
            writer.print(JSON.toJSONString(result));
        } catch (IOException e) {
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
