package com.pixeltribe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 規則：如果 URL 路徑是 /uploads/** ...
        registry.addResourceHandler("/uploads/**")
                // ...就去 file:./uploads/ 這個實體資料夾裡找檔案
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
