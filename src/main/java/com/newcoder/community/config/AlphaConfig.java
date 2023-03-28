package com.newcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

//装配第三方的配置类
@Configuration
public class AlphaConfig {
//    装配别人的bean  方法名就是bean的名字
    @Bean
    public SimpleDateFormat simpleDateFormat(){
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
