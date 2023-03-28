package com.newcoder.community.config;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
//被spring容器装配
    @Bean
    public Producer kaptchaProducer(){
//        为了封装properties数据 ,直接王对象中塞值 去官网看参数
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","40");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ASDFGHJKLZXCVBNMQWERTYUIOP");
        properties.setProperty("kaptcha.textproducer.char.length","4");
//        使用哪个干扰类 变形
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        DefaultKaptcha kaptcha = new DefaultKaptcha();
//        传入一些参数
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
