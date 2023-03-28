package com.newcoder.community.dao;

import com.newcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//打上注解才能自动装配
@Mapper
public interface DiscussPostMapper {
//    分页  这个userID是查我的贴子的，查首页的时候不传入   0的时候不拼，其他值拼，实现一个动态sql
//    offset起始行行号，limit每页多少数据
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);
//查询有多少条数据，用于查询有多少页  param是给参数取一个别名，if动态sql时候，并且只有一个参数，就必须写param

    int selectDiscussPostRows(@Param("userId") int userId);
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);
    int updateScore(int id,double status);
}
