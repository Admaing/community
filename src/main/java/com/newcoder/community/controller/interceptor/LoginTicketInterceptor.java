package com.newcoder.community.controller.interceptor;

import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CookieUtil;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if (ticket!=null){
//            查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            检查凭证是否有效    不为空状态为零，超时时间晚于当前时间
            if (loginTicket!=null && loginTicket.getStatus()==0 &&loginTicket.getExpired().after(new Date())){
//                根据凭证查询用户  在模板上或者后面用
                User user = userService.findUserById(loginTicket.getUserId());
//                在本次请求中持有用户   要考虑多对一（服务器）  线程隔离的stacklocal
//线程到最后才销毁
                hostHolder.setUser(user);
//                构建用户认证的结果并且存入securityContext，以便于Security进行授权.  在请求开始的拦截，在请求结束清理
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(),userService.getAuthorities(user.getId())

                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }
//    用user，在模板引擎之前，把这玩意存到model里


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null && modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

//    还要清掉hostholder

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
