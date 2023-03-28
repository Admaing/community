package com.newcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Event;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event){
        //将事件发布到指定得主题   event转换成json
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
