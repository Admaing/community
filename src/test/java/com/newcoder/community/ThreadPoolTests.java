package com.newcoder.community;


import com.newcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
//测试代码怎么引用主类呢？
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {
//    logger在输出内容的时候，会带上线程的id

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK可定时执行人物的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private void sleep(long m) throws InterruptedException {
        Thread.sleep(m);
    }
//    main中启动线程不down掉  ,main会挡着
    // test 和线程并发， 直接结束

//    1.JDK 普通线程池
    @Test
    public void testExecutorService() throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService");
            }
        };
        for (int i=0; i<10; i++){
            executorService.submit(task);
        }

        sleep(10000);
    }

    // 2, JDK定时任务线程池
    @Test
    public void testScheduled() throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService");
            }
        };
//        以固定频率执行  延迟10000毫秒
        scheduledExecutorService.scheduleAtFixedRate(task, 10000,1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }
//    Spring普通线程池

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
//    可执行定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;


    //Spring普通线程池
    @Test
    public void testThreadPoolTaskExecutor() throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPooltaskService");
            }
        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }

    //Spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler() throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPooltaskService");
            }
        };
        Date startTime = new Date(System.currentTimeMillis()+10000);
        taskScheduler.scheduleAtFixedRate(task, startTime,1000);
        sleep(30000);
    }

    @Autowired
    private AlphaService alphaService;
    //简便调用方式
// spring 普通线程池的简化
    @Test
    public void testThreadPoolTaskExecutorSimple() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            alphaService.execte1();
        }
        sleep(10000);
    }

//    spring 定时任务线程池简化
    @Test
    public void testSpringPool() throws InterruptedException {
//    有程序跑就会自动启动
        sleep(30000);
    }
}

