package com.newcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

//    替换符
    private static final String REPLACEMENT = "***";

//    根节点初始化
    private TrieNode rootNode = new TrieNode();

//    首次访问初始化  postConstrict是一个初始化方法
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                缓冲流
                ){
            String keyword;
            while ((keyword= reader.readLine())!=null){
//                添加到前缀树当中
                this.addKeyword(keyword);
            }
        }catch (IOException e){
            logger.error("加载敏感词文件失败"+e.getMessage());
        }

    }

//    将一个敏感词添加到前缀树当中去
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i=0; i<keyword.length(); i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode==null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
//            指向子节点，进入下一轮循环
            tempNode = subNode;
//            设置结束标识
            if (i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤铭感词
      * @param text 带过滤的文本
     * @return 过滤后的文本
     */

    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

//        指针1
        TrieNode tempNode = rootNode;
//        指针2
        int begin = 0;
//        指针3
        int position = 0;
//      变长字符串 结果
        StringBuilder sb = new StringBuilder();

        while(position < text.length()){
            char c = text.charAt(position);
//            跳过符号
            if (isSymbol(c)){
                //若指针1处于根节点,将此符号计入结果，让指针2向下走一步
                if (tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
//                无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
//          检测下级节点  往下级走
            tempNode = tempNode.getSubNode(c);
            if (tempNode==null){
//                以begin为开头的字符串不是铭感词
                sb.append(text.charAt(begin));
//                进入下一个位置
                position = ++begin;
//                重新指向根节点
                tempNode = rootNode;

            }else if (tempNode.isKeywordEnd()){
//                发现铭感词,将begin开头，position结尾字符串更换为"***"
                sb.append(REPLACEMENT);
//                        进入下一个位置
                begin = ++position;
//                重新指向根界定啊
                tempNode = rootNode;
            }else{
//                检查下一个字符
                position ++ ;
            }
        }
//        将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }
//判断是否为符号
    private boolean isSymbol(Character c){
//      c的判断是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&& (c<0x2E || c>0x9FFF);
    }
//    前缀树
    private class TrieNode{
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

//            子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
//      添加子节点
        public void addSubNode(Character c ,TrieNode node){
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
        //        关键词结束的标识
        private boolean isKeywordEnd = false;

    }
}
