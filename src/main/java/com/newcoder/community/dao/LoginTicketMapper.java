package com.newcoder.community.dao;

import com.newcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
//不推荐使用了
@Deprecated
public interface LoginTicketMapper {
//    缺点是sql复杂的时候看着难受
    @Insert({

            "insert into login_ticket (user_id,ticket,status,expired) ",
//            每句后面+个空格
            "values(#{userId},#{ticket},#{status},#{expired}) "
    })
        //            希望id自动生成 注入到id中
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
//            也支持动态sql
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\">",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket,int status);
}
