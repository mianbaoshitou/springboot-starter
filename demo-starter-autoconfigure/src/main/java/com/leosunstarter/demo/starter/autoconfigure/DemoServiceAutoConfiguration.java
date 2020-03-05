package com.leosunstarter.demo.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DemoServiceProperties.class)
public class DemoServiceAutoConfiguration {
    @Autowired
    DemoServiceProperties demoServiceProperties;

    @Bean
    public DemoService demoService(){
        return  new DemoService();
    }
}
