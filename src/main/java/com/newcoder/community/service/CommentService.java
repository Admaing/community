package com.newcoder.community.service;

import com.newcoder.community.dao.CommentMapper;
import com.newcoder.community.entity.Comment;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

//    因为包含两次操作，所以要事务管理    隔离级别，传播机制
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        //更新评论数量  判断更新的是哪一类评论的是数量
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }

        return rows;
    }
}
