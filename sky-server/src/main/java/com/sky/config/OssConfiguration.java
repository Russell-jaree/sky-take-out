package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类，用于创建aliossutils对象
 * 1. 读取阿里云配置
 * 2. 创建 AliOssUtil 对象
 * 3. 把 AliOssUtil 对象放进 Spring 容器
 * 4. 以后其他类可以直接注入 AliOssUtil
 */
@Configuration //@Component：这是一个普通组件 @Configuration：这是一个专门用来配置和创建其他组件的组件
@Slf4j
public class OssConfiguration {

    @Bean //把这个方法返回的对象交给spring管理
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){

        log.info("开始创建阿里云文件上传工具类：{}",aliOssProperties);
        return  new AliOssUtil(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());
    }
}
