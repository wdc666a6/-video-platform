package com.example.videoplatform.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地物理路径
        String path = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + path);

        // 将 /covers/** 映射到本地物理路径
        String coversPath = System.getProperty("user.dir") + "/covers/";
        registry.addResourceHandler("/covers/**").addResourceLocations("file:" + coversPath);
    }
}