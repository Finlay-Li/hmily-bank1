package com.dodou.scaffold;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableAspectJAutoProxy
public class HmilyBank1Application {

    public static void main(String[] args) {
        SpringApplication.run(HmilyBank1Application.class, args);
    }

}
