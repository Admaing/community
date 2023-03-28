package com.newcoder.community.controller;

import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//省略访问路径，直接访问方法
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/",method = RequestMethod.GET)
    public String root(){
        return "forward:/index";
    }
//    String表示返回视图名字
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page
    ,@RequestParam(name="orderMode",defaultValue = "0") int orderMode){
//        方法调用之前，springmvc会自动实例化model和page，并将page注入model，
//        所以在thymeleaf中可以直接访问page对象中数据
        page.setRows(discussPostService.findDiscussPostRows(0));//首页
//        orderMode拼上
        page.setPath("/index?orderMode="+orderMode);


        List<DiscussPost> list = discussPostService.
                findDiscussPosts(0,page.getOffset(), page.getLimit(),orderMode);
//        一个post对应一个user
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list!=null){
            for (DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }


//    提前把五百页面配置一下
    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }

}
