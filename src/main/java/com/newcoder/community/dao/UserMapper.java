package com.newcoder.community.dao;

import com.newcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

//加注解让 spring装配，mybatis单独的Mapper,也可以用responsity
@Mapper
public interface UserMapper {

    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
//    返回插入行数
    int insertUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);
}
