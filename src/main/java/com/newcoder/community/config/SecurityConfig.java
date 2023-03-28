package com.newcoder.community.config;

import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
//        静态资源可以直接访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
//                        拥有以下任意权限就能访问
                        AUTHORITY_ADMIN,
                        AUTHORITY_USER,
                        AUTHORITY_MODERATOR
                )
//                除了以上的，统统都允许
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够时的处理
        http.exceptionHandling()
//                没有登陆时怎么处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
//                        判断当前请求同步还是异步， 看请求头
                        String xRequestedWith = request.getHeader("x-requested-with");
//
                        if ("XMLHttpRequest".equals(xRequestedWith)){
//                            异步
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登陆"));

                        }else{
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
//                权限不足怎么处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
//
                        if ("XMLHttpRequest".equals(xRequestedWith)){
//                            异步
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限"));

                        }else{
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
//        自动拦截logout处理 退出 底层使用Filter；filter在controller之前
        // Security底层默认拦截logout ，尽心退出处理
        // 覆盖默认逻辑，才能执行我们的退出代码   没听懂这段代码的逻辑
        http.logout().logoutUrl("/securitylogout");
    }
}
