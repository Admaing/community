package com.newcoder.community.util;

import com.newcoder.community.entity.User;
import org.springframework.stereotype.Component;

//持有用户的信息，用于代替session对象的, 线程隔离的
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }
//    清理
    public void clear(){
        users.remove();
    }
}
