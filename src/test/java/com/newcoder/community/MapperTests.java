package com.newcoder.community;

import com.newcoder.community.CommunityApplication;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.MessageMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
//测试代码怎么引用主类呢？
@ContextConfiguration(classes = CommunityApplication.class)
//哪个类想得到spring容器就实现
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User(){};
//        User user=userMapper.insertUser();
    }

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testSelcetPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10,0);
        for (DiscussPost post:list){
            System.out.println(post);
        }
        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("123312");
        loginTicket.setId(1);
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("123312");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("123312",0);
    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111,0,20);
        for (Message message:list){
            System.out.println(message);
        }
       int count =  messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112",0,10);
        for (Message message:list){
            System.out.println(message);
        }
        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count  = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }

    @Test
    public void testInsertLetters(){
//        Message message = new Message();
//        message.setCreateTime(new Date());
//        message.setContent("wasd");
//        message.setConversationId("111_112");
//        message.setToId(112);
//        message.setFromId(111);
////        message.set
//        int a = messageMapper.insertMessage(message);
//        System.out.println(a);
        List<Integer> list = new ArrayList<>();
        list.add(355);
        list.add(356);
        messageMapper.updateStatus(list,1);
    }
}
