package com.newcoder.community.controller.advice;

import com.newcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//会扫描 指定范围的bean   扫描带有controller注解的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger Logger = LoggerFactory.getLogger(ExceptionAdvice.class);
//    处理所有的异常的方法  处理哪些异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger.error("服务器发生异常: "+e.getMessage());
//        遍历错误栈的信息
        for (StackTraceElement element : e.getStackTrace()){
            Logger.error(element.toString());
        }

//        要区分是同步还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");
        //            返回普通字符串  需要手动转换成json
        if ("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        }else{
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
