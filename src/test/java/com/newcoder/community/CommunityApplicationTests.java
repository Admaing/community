package com.newcoder.community;

import com.newcoder.community.CommunityApplication;
import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
//测试代码怎么引用主类呢？
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
//哪个类想得到spring容器就实现
class CommunityApplicationTests implements ApplicationContextAware {

//	加一个成员变量记录spring容器
	private ApplicationContext applicationContext;
	@Test
	void contextLoads() {
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Test
	public void testApplicationContext(){
		System.out.println(applicationContext);
//		怎么用这玩意管理bean 获取
		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
		System.out.println(alphaDao.select());
		alphaDao = applicationContext.getBean("alphaHibernate",AlphaDao.class);
		System.out.println(alphaDao);

	}

	@Test
	public void TestBeanManagement(){
//		被容器管理的bean ，是单例的
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
		alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	@Test //主动获取是一种笨拙的方式
	public void testBeanConfig(){
		SimpleDateFormat simpleDateFormat =
				applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}

//	依赖注入
//希望spring 容器把alphaDao 注入给这个属性
	@Autowired
	@Qualifier("alphaHibernate")  //不用默认的  也可以通过构造器注入，一般直接通过属性注入
	private AlphaDao alphaDao;

	@Autowired
	private AlphaService alphaService;

	@Autowired
	private SimpleDateFormat simpleDateFormat;

	@Test //DI 依赖注入
	public void testDI(){
		System.out.println(alphaDao.getClass());
	}
}
