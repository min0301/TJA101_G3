package com.pixeltribe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private LoginInterceptor loginInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginInterceptor).addPathPatterns("/api/cart/**"); // 這個指攔截購物車相關的API

	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String rootDir = System.getProperty("user.dir"); // 取得專案根目錄
		registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + rootDir + "/uploads/");
	}
}
