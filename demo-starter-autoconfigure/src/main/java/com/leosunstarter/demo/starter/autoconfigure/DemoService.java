package com.leosunstarter.demo.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;

public class DemoService {
    @Autowired
    DemoServiceProperties demoServiceProperties;
    public String greeting(String name){
        return demoServiceProperties.getPrefix() + name + demoServiceProperties.getSuffix();
    }
}
