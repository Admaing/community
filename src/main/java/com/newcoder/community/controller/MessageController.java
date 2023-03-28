package com.newcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

//    查当前用户的私信
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;
    //私信列表
    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){

        //分页信息
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if (conversationList!=null){
            for (Message message:conversationList){
                Map<String ,Object> map = new HashMap<>();
                map.put("conversation",message);
//                未读消息数量
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
//                会话包含的消息
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
//                显示当前用户对话的人的头像
                int targetId = user.getId()==message.getFromId() ? message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
//        查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        System.out.println("成功");
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        //分页信息设置
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if (letterList!=null){
            for (Message message:letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

//        补充来自 xxxx的私信，当前登陆与目标的名字  查询私信目标

        model.addAttribute("target",getLetterTarget(conversationId));

//        私信列表中信息提取，然后设置成已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }


    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if (letterList!=null){
            for (Message message : letterList){
//                是接收者，才能读
                if (hostHolder.getUser().getId()==message.getToId()&& message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int d0 = Integer.parseInt(ids[0]);
        int d1 = Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId()==d0){
            return userService.findUserById(d1);
        }else{
            return userService.findUserById(d0);
        }
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target ==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
//        会话id，小的在前大的灾后
        if (message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
//        message.setStatus(0);
        message.setCreateTime(new Date());
        int q = messageService.addMessage(message);
        System.out.println(q);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
//        用来封装聚合数据

        if (message!=null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            int count  = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("unread",unread);
            model.addAttribute("commentNotice",messageVO);
        }

        //查询点赞类通知

        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
//        用来封装聚合数据

        if (message!=null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));
            int count  = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);
            messageVO.put("unread",unread);
            model.addAttribute("likeNotice",messageVO);
        }

        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
//        用来封装聚合数据

        if (message!=null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
//            messageVO.put("postId",data.get("postId"));
            int count  = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("unread",unread);
            model.addAttribute("followNotice",messageVO);
        }

//        查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic,Page page,Model model){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(),topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if (noticeList!=null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //存内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));

                //通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);
        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }


}
