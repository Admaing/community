package com.newcoder.community.controller;

import com.newcoder.community.entity.Comment;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.Event;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.service.CommentService;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.HostHolder;
import com.newcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
//得到当前用户id
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
//        评论给谁,
        comment.setUserId(hostHolder.getUser().getId());
//        表示有效的
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件  帖子id因为不明显，所以放到map当中
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);

        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if (comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);
//        触发发帖事件 评论给帖子的时候
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
//                    .setData("postId",discussPostId);
            eventProducer.fireEvent(event);
            //        计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
//        去重且不关注顺序
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }



        return "redirect:/discuss/detail/"+discussPostId;
    }
}
