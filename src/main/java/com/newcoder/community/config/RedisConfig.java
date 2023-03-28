package com.newcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.annotation.Resource;

@Configuration
public class RedisConfig {
//    定义第三方的bean
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        // 设置key的序列化方式
//        template.setKeySerializer(RedisSerializer.string());
//        // 设置value的序列化方式
//        template.setValueSerializer(RedisSerializer.json());
//        // 设置hash的key的序列化方式
//        template.setHashKeySerializer(RedisSerializer.string());
//        // 设置hash的value的序列化方式
//        template.setHashValueSerializer(RedisSerializer.json());
//
//        template.afterPropertiesSet();
//        return template;
//    }
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
//        连接工厂注入 自动注入参数bean
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
//        设置普通value的序列化方式  json格式结构化，好识别
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
//        触发生效
        template.afterPropertiesSet();
        return template;

    }
}
