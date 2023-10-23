package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private ProjectInterceptor projectInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //不拦截的url
        final String commonExclude[] = {"/page/*","/css/*","/js/*","/img/*","/my/*"};

        registry.addInterceptor(projectInterceptor).excludePathPatterns(commonExclude);
        registry.addInterceptor(projectInterceptor).addPathPatterns("/*");
    }
}
