package com.newcoder.community.controller;

import com.newcoder.community.service.AlphaService;
import com.newcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha") //通过这个名字访问类
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring boot";
    }
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

//在spring mvc怎么获得请求对象
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
//        通过response对象，直接给浏览器返回响应数据
//        获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+":"+value);
        }
        System.out.println(request.getParameter("code"));
//        返回响应数据
        response.setContentType("text/html;charset=utf-8");

        try(
                PrintWriter writer = response.getWriter();
                ) {

            writer.write("<h1>牛客网</h1>");
        }catch (IOException e){
            e.printStackTrace();;
        }

    }

//    GET请求处理  默认GET请求
    // 查询所有学生 分页，students?current=1&limit=20  这回只能请求GET
    @RequestMapping(path = "/students",method= RequestMethod.GET)
    @ResponseBody
    public String getStudents(
//            设置默认参数注入
            @RequestParam(name="current",required = false,defaultValue = "1") int current,
            @RequestParam(name="limit",required = false,defaultValue = "10")int limit){
System.out.println(current);
System.out.println(limit);
        return "some students";
    }
//   查询一个学生，根据学生id /student/123  ，直接把参数编到路径当中
    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
//    从路径得到变量
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }
//    处理POST请求  表单 /alpha/student
    @RequestMapping(path="/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "save scueess";
    }

//    响应html数据
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        System.out.println("sads");
        mav.addObject("name","张三");
        mav.addObject("age",30);
//        设置模板路径和名字
        mav.setViewName("/demo/view");
        return mav;
    }

//    和上面方式差不多
    @RequestMapping(path="/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","asd");
//        model.addAttribute()
        return "/demo/view";
    }

//    除了相应Html 还能相应json数据(异步请求当中)
//    Java对象返回给浏览器  ->JSON字符串,->浏览器解析对象成JS对象

//    @RequestMapping(path="/emp",method = RequestMethod.GET)
//    @ResponseBody
//    public Map<String,Object> getEmp(){
//        Map<String,Object> emp = new HashMap<>();
//        emp.put()
//
//    }

    //cookie示例  放在头里，和返回什么没什么关系
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
//    创建cookie，存到response
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
//        设置cookie生效范围  (访问哪些路径才会发cookie，每次都发是占用服务器资源的)
        cookie.setPath("/community/alpha");
        //默认存到内存里，浏览器关掉就没有惹，设置cookie生存时间，存在硬盘当中
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
//    如果使用request还要遍历
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }

    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
//        springmvc自动创建session，类似Model
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set Session";
    }

    @RequestMapping(path = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }


//    ajax实例
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }



}
