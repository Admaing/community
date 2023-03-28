package com.newcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

//因为获取bean的时候是根据接口获取，这两个接口一样，因此加个优先级  想替换的时候
@Primary
@Repository
public class AlphaDaoMyBatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "mybatis>";
    }
}
