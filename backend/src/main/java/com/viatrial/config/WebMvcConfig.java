package com.viatrial.config;

import com.viatrial.security.WriteAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final WriteAccessInterceptor writeAccessInterceptor;

    public WebMvcConfig(WriteAccessInterceptor writeAccessInterceptor) {
        this.writeAccessInterceptor = writeAccessInterceptor;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/paper").setViewName("forward:/index.html");
        registry.addViewController("/paper/").setViewName("forward:/index.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 只拦截写接口所在前缀，静态资源与页面转发不受影响。
        registry.addInterceptor(writeAccessInterceptor).addPathPatterns("/api/**");
    }
}
