package com.example.module.login;

import com.example.common.Code;
import com.example.common.Result;
import com.example.dao.UserDao;
import com.example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 功能: 登录验证
 * 作者: PadoPhyllis
 * 日期: 2023.10.11
 */
@RestController
@RequestMapping("/my")
public class LoginController {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserDao userDao;

    @PostMapping("login")
    public Result login(HttpServletRequest request, @RequestBody Map<String,String> map) {
        //登录限流
        LimitCache limitCache = LimitCache.getInstance();
        LinkedList<LocalDateTime> queue = null;

        //获取ip地址
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        //先判断ip地址是否禁止登录
        LocalDateTime forbiddenTime = limitCache.getForbiddenMap().get(ip);

        if (forbiddenTime != null) {
            Long after = ChronoUnit.MINUTES.between(forbiddenTime, LocalDateTime.now());
            if (after <= 15) {
                return new Result(Code.LOGINFAIL,null,"距离解禁时间还未超过15分钟！");
            }else {
                limitCache.getForbiddenMap().clear();
            }
        }

        //如果是首次登录，则创建队列
        if (limitCache.getLoginMap().get(ip) == null){
            queue = new LinkedList<>();
        }else {
            queue = LimitCache.getLoginMap().get(ip);
        }

        //登录次数达到登录次数上限
        if (queue.size() == 5){
            // 当前时间和队列中最早的登录时间比较 是否小于15分钟
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime firstLoginTime = queue.poll();
            Long duration = ChronoUnit.MINUTES.between(firstLoginTime, now);

            if (duration <= 15) {
                // 禁止该IP登录
                limitCache.getLoginMap().clear();
                limitCache.getForbiddenMap().put(ip, now);
                return new Result(Code.LOGINFAIL,null,"失败次数超过5次,请您15分钟后再尝试");
            }
        }

        String username = map.get("userName");
        String password = map.get("password");
        User user = userDao.selectByName(username);

        if (user == null) {
            queue.offer(LocalDateTime.now());
            limitCache.getLoginMap().put(ip, queue);
            return new Result(Code.LOGINFAIL,null,"该用户不存在，请联系管理员开通账户！");
        }

        if (password.equals(user.getPassword())){
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("id",String.valueOf(user.getId()));
            tokenMap.put("username",user.getUsername());
            tokenMap.put("password", user.getPassword());
            String token = JWTUtils.getToken(tokenMap);
            return new Result(Code.LOGINSUCCESS, token, "登录成功！");
        }

        queue.offer(LocalDateTime.now());
        limitCache.getLoginMap().put(ip, queue);
        return new Result(Code.LOGINFAIL,null,"用户名或者密码错误！");
    }
}