package com.newcoder.community.service;

import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.MailClient;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired //模板引擎
    private TemplateEngine templateEngine;

//    注入固定的值
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);

        }
        return user;
    }
//希望返回一个很多条件的，所以用map
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
//        先判断是否空值

        if (user==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        if (StringUtils.isBlank(user.getUsername())){
            map.put("username","账号不能为空");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())){
            map.put("username","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("username","邮箱不能为空");
            return map;
        }

//        验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u!=null){
            map.put("usernameMsg","该账号已经存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u!=null){
            map.put("mailMsg","改邮箱已经被注册");
            return map;
        }
/**
 * 对User+salt  设置用户status 头像和创建时间
 */
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        int id = userMapper.insertUser(user); //会把id赋值回来
//        发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
//        http://localhost:8080/community/activation/101/code   激活路径
        String url = domain + contextPath + "/activation/" + user.getId() + "/" +user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return null;
    }
//    链接点的第一次才有用，激活码是否有用，重复激活
//    传id,激活码
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus()==1){
            return ACTIVATION_REAPEAT;
        }else if (user.getActivationCode().equals(code)){
//            user.setStatus(1);
            clearCache(userId);
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }


//    @Autowired
//    LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    public Map<String,Object> login(String username,String password, int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        //先空值处理

        if (StringUtils.isBlank(username)){
            map.put("usernameMsp","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        //验证账号;
        User user = userMapper.selectByName(username);
        if (user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        if (user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }
//        验证mima
        password = CommunityUtil.MD5(password+user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }

//        生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }


    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl){

//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    //2.取不到时， 就初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.当数据变更时，清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}
