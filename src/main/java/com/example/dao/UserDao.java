package com.example.dao;

import com.example.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserDao {
    User selectByName(String username);

    List<User> selectAll();
}
