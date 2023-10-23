package com.example.module.login;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 功能：登录限流
 * 作者：PadoPhyllis
 * 日期：2023.10.23
 */
public class LimitCache {
    private static volatile LimitCache instance;
    // 记录登录的ip地址及每次登录时间
    private static HashMap<String, LinkedList<LocalDateTime>> loginMap = new HashMap<>();
    // 记录禁止登录的ip地址及禁止开始时间
    private static HashMap<String, LocalDateTime> forbiddenMap = new HashMap<>();

    // 构造器私有化，不能在类的外部随意创建对象
    private LimitCache() {
    }
    // 提供一个全局的访问点来获得这个"唯一"的对象
    public static LimitCache getInstance() {
        if (instance == null) {
            synchronized (LimitCache.class) {
                if (instance == null) {
                    instance = new LimitCache();
                }
            }
        }
        return instance;
    }

    public static HashMap<String, LinkedList<LocalDateTime>> getLoginMap() {
        return loginMap;
    }
    public static HashMap<String, LocalDateTime> getForbiddenMap() {
        return forbiddenMap;
    }
}