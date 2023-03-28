package com.newcoder.community;

import com.newcoder.community.CommunityApplication;
import com.newcoder.community.service.AlphaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
//测试代码怎么引用主类呢？
@ContextConfiguration(classes = CommunityApplication.class)
public class TransationTests {
    @Autowired
    private AlphaService alphaService;

    @Test
    public void testSave1(){
        Object obj = alphaService.save1();
        System.out.println(obj);
    }
//    编程式事务

    @Test
    public void testSave2(){
        Object obj = alphaService.save2();
        System.out.println(obj);
    }
}
