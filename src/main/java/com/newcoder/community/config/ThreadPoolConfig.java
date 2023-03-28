package com.newcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
//启动定时任务
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
