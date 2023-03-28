package com.newcoder.community;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
//测试代码怎么引用主类呢？
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTest {
    @Autowired
    private Scheduler scheduler;

    @Test
    public void TestDeleteJob() throws SchedulerException {
       boolean result =  scheduler.deleteJob(new JobKey("alphaJob","alphaJobGroup"));
        System.out.println(result);
    }

}
