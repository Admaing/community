package com.newcoder.community;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @BelongsProject: community
 * @BelongsPackage: com.newcoder.community
 * @author: link_g
 * @date: 2023/3/27 9:23
 * @Description:
 */
public class CommunityServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
        return builder.sources(CommunityApplication.class);
    }
}
