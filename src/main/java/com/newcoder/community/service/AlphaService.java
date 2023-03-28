package com.newcoder.community.service;

import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.beans.Transient;
import java.util.Date;

@Service
//@Scope("prototype") //多个实例 不经常用
public class AlphaService {
    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);
    @Autowired
    private AlphaDao alphaDao;

    public String find(){
        return  alphaDao.select();
    }

    public AlphaService(){
        System.out.println("实例化");
    }
    @PostConstruct //会在构造器之后调用
    public void init(){
        System.out.println("初始化server");
    }

    @PreDestroy  //销毁对象之前调用
    public void destroy(){
        System.out.println("销毁");
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;
//    要保证一个事务性  默认隔离方式 ，加参数   事务传播机制:有可能调用另外业务方法，另外的业务方法也可能有事务注解，那是以A为准还是以B为准
    //Required  支持当前事务，a调B，a是外部事务（当前），如果外部事务不存在则创建新事务
    // REQUIRES_NEW   :创建一个新的事务，并且暂停当前事务（外部事务）
    //NESTED  :   如果当前存在事务（外部是），则嵌套在该事务中执行(独立的提交和回滚)，如果不存在，就会和required一样
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
//          新增用户
//        新增帖子
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
//        insert之后 mybatis会给他插入id
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("是你爹");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

//        报错看事务是否回滚
        Integer.valueOf("abc");

        return "ok";
    }

//    让该方法在多线程的环境下，被异步调用。
    @Async
    public void execte1(){
        logger.debug("execute1");
    }

//    定时 延迟多久   多长频率执行一次
//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
//    public void execute2(){
//        logger.debug("execute2");
//    }


    @Autowired
    private TransactionTemplate transactionTemplate;
    public Object save2(){
//        设置传播机制
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus status) {
                //          新增用户
//        新增帖子
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.MD5("123"+user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
//        insert之后 mybatis会给他插入id
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hellnih");
                discussPost.setContent("是你爹");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);
//        报错看事务是否回滚
                Integer.valueOf("abc");

                return "ok";
//                return null;
            }
        });
    }

}
