package com.newcoder.community.ascpect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
//    定义切点 织入到bean的哪些位置  所有service里面的所有方法的所有的参数所有的返回值都要处理
    @Pointcut("execution(* com.newcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }
    @AfterReturning("pointcut()")
    public void afterReturing(){
        System.out.println("afterReturing");
    }
    @AfterThrowing("pointcut()")
    public void afterthrowting(){
        System.out.println("afterthrowting");
    }
//在前后 同时处理
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint)throws Throwable{
        System.out.println("前面");
//        根据链接点前后处理  调用原始对象方法
        Object object = joinPoint.proceed();
        System.out.println("后面");
        return object;

    }
//    在返回值
//    以后
//    @AfterReturning
//    拍一场
}
