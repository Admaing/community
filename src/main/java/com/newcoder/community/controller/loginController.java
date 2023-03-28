package com.newcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class loginController implements CommunityConstant {

    @Autowired
    private UserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;


    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
//        返回模板
        return "/site/register";
    }
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
//        返回模板
        return "/site/login";
    }


    private static final Logger logger =  LoggerFactory.getLogger(loginController.class);


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Producer kaptcharProducer;
//    像浏览器返回图片路径  手动response输出  跨请求  服务端需要记住验证码，请求的时候才能验证，是敏感数据所以使用session
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptchar(HttpServletResponse response/*, HttpSession session*/) {
//        生成验证码
        String text = kaptcharProducer.createText();
        BufferedImage image = kaptcharProducer.createImage(text);

//        将验证码存入session
//        session.setAttribute("kaptcha",text);

        //验证码归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //验证码存到redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner); //        设置有效时间
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

//        将图片输出给浏览器
        response.setContentType("image/png");
        //获取字节流
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        }catch (IOException e){
            logger.error("响应验证码失败");
        }

//    捕获异常
    }

    @RequestMapping(path="/register",method = RequestMethod.POST)
    public String register(Model model, User user){
//        因为要等待几秒，跳到哪个页面，要把这个信息传进去
        Map<String,Object> map = userService.register(user);
        if (map ==null|| map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一篇激活邮件");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }
    @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
//    从路径当中国取值
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if (result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号可以正常使用");
            model.addAttribute("target","/login");
        }else if (result==ACTIVATION_REAPEAT){
            model.addAttribute("msg","无效操作，该账号已经激活");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，激活码不正确");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

//session是取验证码的   如果参数是user对象，会直接装到model中，普通参数不会装进去   或者在request当中
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,
                        Model model/*, HttpSession session*/,HttpServletResponse response,
                        @CookieValue("kaptchaOwner")String kaptchaOwner){

//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
//        忽略大小写
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        //检查账号密码  传入过期时间；如果勾上记住我，
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
//            有效路径
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

//    要求mvc把ticket注入进来
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
//        重定向默认GET请求  注销要清空
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
