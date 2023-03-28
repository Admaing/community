package com.newcoder.community;

import com.newcoder.community.CommunityApplication;
import com.newcoder.community.util.MailClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SpringBootTest
//测试代码怎么引用主类呢？
@ContextConfiguration(classes = CommunityApplication.class)
//哪个类想得到spring容器就实现
public class MailTests {
    @Autowired
    private MailClient mailClient;

//    直接注入模板引擎
    @Autowired
    public TemplateEngine templateEngine;

    @Test
    public void testTextMail(){
        mailClient.sendMail("471218449@qq.com","Test","welcome");
        //发送html邮件
    }
    @Test
    public void testMail(){
//        context和go里面的有什么区别
        Context context = new Context();
        context.setVariable("username","我是你爹");
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("471218449@qq.com","HTML",content);
    }
    @Test
    public void test(){
        List list = new ArrayList<>();
        list.add("Hello");
        list.add("Learn");
        list.add("Hello");
        list.add("Welcom");
        Set set = new HashSet<>();
        set.addAll(list);
        System.out.println(set.size());
    }
}
