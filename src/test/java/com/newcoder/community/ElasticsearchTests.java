package com.newcoder.community;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.newcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
//测试代码怎么引用主类呢？
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
//    从Mysql中取到，然后转存
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));

    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水");
        discussPostRepository.save(post);
    }
    @Test
    public void testDelete(){
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository(){
//        匹配词变成高亮词  with构建搜索条件  按照type  create_time排序
        SearchQuery searchQuery =  new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title")
                                .preTags("<em>")
                                .postTags("</em>"),
                        new HighlightBuilder.Field("content")
                                .preTags("<em>")
                                .postTags("</em>"))
                .build();
//        page是人家的page  返回两份数据，

//        elasticsearchTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        //底层获取到了高亮现实的值，但是没有返回。 因此不用search方法
        Page<DiscussPost> page =  discussPostRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page){
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery =  new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title")
                                .preTags("<em>")
                                .postTags("</em>"),
                        new HighlightBuilder.Field("content")
                                .preTags("<em>")
                                .postTags("</em>"))
                .build();
//        匿名实现接口
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                //createTime
//                结果会进入到response
                SearchHits hits = response.getHits();
                if (hits.getTotalHits()<=0){
                    return null;
                }
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit:hits){
                    DiscussPost post = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));
                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);
                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));
//
//
                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    //处理高亮显示
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField!=null){
//                        titlepi匹配的词条有多个
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField!=null){
//                        titlepi匹配的词条有多个
                        post.setTitle(contentField.getFragments()[0].toString());
                    }
                    list.add(post);
                }
                return new AggregatedPageImpl(list,pageable,
                        hits.getTotalHits(),response.getAggregations(),response.getScrollId(),hits.getMaxScore());
            }
        });
//        Page<DiscussPost> page =  discussPostRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page){
            System.out.println(post);
        }
    }
}
