package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充
 */

@Aspect //表示这是一个切面类，切面类通常要包含：拦截哪些方法、方法执行前要做什么、执行后要做什么、出现异常时要做什么。
@Component //让Spring创建并管理afa对象，当一个类需要交给spring管理，但是他不属于明确的业务层、管理层等
@Slf4j
public class AutoFillAspect {

    //切入点，就是对哪些类的哪些方法进行拦截

    //切点表达式
    /**
     * execution(* com.sky.mapper.*.*(..))表示拦截mapper包下的所有类的所有方法
     * 第一个 *       返回值任意
     * com.sky.mapper 包名
     * 第一个 *       任意 Mapper 类
     * 第二个 *       任意方法
     * (..)           参数任意
     *&& @annotation(com.sky.annotation.AutoFill) 表示只有加了AFl的才会被拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){

    }

    /**
     * 前置通知，在通知中进行公共字段的赋值
     * 表示在执行AFPC之前先执行AF
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段的自动填充....");

        //获取到数据库操作类型，是update还是insert
         MethodSignature signature =(MethodSignature) joinPoint.getSignature(); //方法签名对象
        //joinPoint.getSignature()是获取方法签名，方法签名包含：方法名+参数类型，joinpoint是spring自动创建的，
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获得方法上的注解对象
        //这里的signature.getMethod()就是Java的反射，得到的是这种EmployeeMapper.insert(Employee)
        //getAnnotation(AutoFill.class)这个是：到这个方法身上，查找类型为 AutoFill 的注解，并返回一个AF对象。
        OperationType operationType = autoFill.value(); //获得数据库操作类型

        //获取当前被拦截的方法的参数，也就是实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return ;
        }

        Object entity =  args[0] ;
        //为实体对象的公共值统一赋值，也就是准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前的操作类型，为对应的属性来赋值，利用反射来完成
        if(operationType == OperationType.INSERT){
            //插入操作，四个都要赋值，利用反射
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod( AutoFillConstant.SET_CREATE_TIME , LocalDateTime.class);
                //从当前实体类中，找到名称为 setCreateTime，参数类型为 LocalDateTime 的方法。
                Method setCreateUser = entity.getClass().getDeclaredMethod( AutoFillConstant.SET_CREATE_USER , Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //利用反射来赋值
                setCreateTime.invoke(entity,now);
                //method.invoke(目标对象, 方法参数);等价于entity.setCreateTime(now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
               e.printStackTrace();
            }

        } else if (operationType == OperationType.UPDATE) {
            //为两个赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod( AutoFillConstant.SET_UPDATE_TIME , LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod( AutoFillConstant.SET_UPDATE_USER , Long.class);

                //利用反射来赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
