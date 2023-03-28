package com.newcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.Message;
import com.newcoder.community.service.DiscussPostService;
import com.newcoder.community.service.ElasticsearchService;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

//    @Autowired
//    private KafkaTemplate kafkaTemplate;
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消息内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event==null){
            logger.error("消息格式错误");
            return ;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityId());
        message.setConversationId(event.getTopic());
//        message.setStatus(0);
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityId());
        content.put("entityId",event.getEntityId());
        //不方便存得，存到content中
        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry: event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());

            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消息内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event==null){
            logger.error("消息格式错误");
            return ;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            logger.error("消息内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event==null){
            logger.error("消息格式错误");
            return ;
        }
//        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

}
