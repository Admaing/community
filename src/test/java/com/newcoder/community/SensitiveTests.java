package com.newcoder.community;

import com.newcoder.community.CommunityApplication;
import com.newcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
//测试代码怎么引用主类呢？
@ContextConfiguration(classes = CommunityApplication.class)
//哪个类想得到spring容器就实现
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "这里面可以😊赌😊博😊,可以😊嫖😊娼😊，可以😊吸😊毒😊，可以开票";
        System.out.println(sensitiveFilter.filter(text));
    }
}
