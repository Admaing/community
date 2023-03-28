package com.newcoder.community.dao;

import jdk.jfr.Registered;
import org.springframework.stereotype.Repository;
//定义bean名字
@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao{

    @Override
    public String select() {
        return "Hibernate";
    }
}
