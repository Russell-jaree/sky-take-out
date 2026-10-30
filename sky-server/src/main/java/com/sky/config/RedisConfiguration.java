package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.naming.NamingEnumeration;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

        log.info("开始创建Redis模板对象..") ;
        //RedisConnectionFactory 这是个链接工厂对象
        RedisTemplate redisTemplate = new RedisTemplate();

        //设置连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate ;

    }
}
