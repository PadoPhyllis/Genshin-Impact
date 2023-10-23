package com.example.module.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Map;

/**
 * 功能：Excel处理工具类
 * 作者：PadoPhyllis
 * 日期：2023.10.9
 */
public class JWTUtils {
    //密钥
    private static final String SING = "技术宅拯救世界！";

    /**
     * 生成token
     * @param map 登录信息
     * @return token字符串
     */
    public static String getToken(Map<String,String> map){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,1); //默认一天过期

        //创建jwtbuilder
        JWTCreator.Builder builder = JWT.create();
        map.forEach((k,v)->{
            builder.withClaim(k,v);
        });

        //设置过期时间
        String token = builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(SING)); //sign
        return token;
    }

    /**
     * 验证token
     * @param token token字符串
     */
    public static DecodedJWT verify(String token){
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }
}
