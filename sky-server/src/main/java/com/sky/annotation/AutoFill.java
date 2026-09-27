package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某方法需要进行的字段自动填充处理
 */
//TODO 这两条注释是固定写法，去了解什么含义
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//程序运行时仍然保留这个注解
//TODO 注解的格式要了解
public @interface AutoFill {
    //指定数据库操作类型：update Insert
    OperationType value(); // 表示这个注解必须传入一个操作类型
}
