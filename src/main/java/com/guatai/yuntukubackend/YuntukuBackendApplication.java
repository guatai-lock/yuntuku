package com.guatai.yuntukubackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication()
@MapperScan("com.guatai.yuntukubackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class YuntukuBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuntukuBackendApplication.class, args);
    }

}
