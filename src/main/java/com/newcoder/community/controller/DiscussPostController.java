package com.newcoder.community.controller;
import com.newcoder.community.entity.*;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.service.CommentService;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.LikeService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import com.newcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){

        User user = hostHolder.getUser();
        if (user==null){
            return CommunityUtil.getJSONString(403,"尚未登陆");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
//        报错情况，将来统一处理
        //        触发发帖事件
        Event event= new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
//        计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
//        去重且不关注顺序
        redisTemplate.opsForSet().add(redisKey,post.getId());

        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;


    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)  //最后spring mvc会把这个bean存入model中
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId , Model model, Page page){
        //帖子   先把具体帖子拿出来
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);

        model.addAttribute("post",post);
//      用关联查询效率更高       但是有冗余s
//        做处理
//        帖子的作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
//        点赞  帖子的点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
//        用户不等录的时候 默认不点赞 点赞状态
        int likeStatus = hostHolder.getUser()==null?0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        System.out.println(likeStatus);
        model.addAttribute("likeStatus",likeStatus);
//      评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());
//        评论：给帖子得评论
//        回复：给评论得评论
//        评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
//        评论显示对象列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList!=null){
            for(Comment comment: commentList){
                Map<String,Object> commentVo = new HashMap<>();
//                评论
                commentVo.put("comment",comment);
//                作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
//                帖子也有评论， 评论也有评论
//                回复列表
                List<Comment> replyList =  commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //        点赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
//        用户不等录的时候 默认不点赞 点赞状态
                likeStatus = hostHolder.getUser()==null?0:
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);
                model.addAttribute("likeStatus",likeStatus);
//                回复得显示对象列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyVoList!=null){
                    for (Comment reply:replyList){
                        Map<String,Object> replyVo = new HashMap<>();
//                        回复
                        replyVo.put("reply",reply);
//                        作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
//                        点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
//        用户不等录的时候 默认不点赞 点赞状态
                        likeStatus = hostHolder.getUser()==null?0:
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);
                        // 回复得目标是否为空
                      User target = reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                       }
                }
                commentVo.put("replys",replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

//    置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        //1是置顶
        discussPostService.updateType(id,1);
        //最新帖子同步到elasticisearch  再触发发帖事件
        Event event= new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    //    加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        //1是加精
        discussPostService.updateStatus(id,1);
        //最新帖子同步到elasticisearch  再触发发帖事件
        Event event= new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //        计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
//        去重且不关注顺序
        redisTemplate.opsForSet().add(redisKey,id);
        return CommunityUtil.getJSONString(0);
    }
    //    删除
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        //1是加精
        discussPostService.updateStatus(id,2);
        //从es删除帖子
        Event event= new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

}
