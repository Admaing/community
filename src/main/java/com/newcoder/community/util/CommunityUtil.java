package com.newcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

//    生成随机字符串，激活码，上传文件等等，生成随机名字
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("_"," ");
    }
//    MD5加密 hello(黑客有库)+随机字符串 -> asdqwq32vx key为原始密码
    public static String MD5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

//    处理json字符串的方法
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            for (String key:map.keySet()){
                json.put(key,map.get(key));
            }
        }

        return json.toJSONString();
    }

    public static String getJSONString (int code,String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString (int code){
        return getJSONString(code,null,null);
    }

    public static void main(String []args){
        Map<String,Object> map = new HashMap<>();
        map.put("name","zhan");
        map.put("age","23");
        System.out.println(getJSONString(0,"ok",map));
    }
}
