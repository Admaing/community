package com.newcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

//该注解表示配置文件  扫描配置类com.newcoder 得有controller，service,component,repository
// 上的注解才会被扫描,区别是业务组件，还是请求组件，容器中的bean是怎么来的（注解就是产生bean）
@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		//解决netty启动冲突的问题  在 Netty4Utils中查找到 redis底層和 es底层均使用netty同时启动有问题
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
//		启动tomcat 自动创建spring容器  创建以后扫描bean
		Map<Integer,Integer> map = new HashMap<>();
		SpringApplication.run(CommunityApplication.class, args);
	}
//要作为web项目启动 ，tomcat就有main方法
}
