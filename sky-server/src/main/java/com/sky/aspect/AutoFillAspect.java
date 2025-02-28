package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，拦截公共字段自动填充操作
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    
    /**
     * 切入点，拦截mapper包下的更新与新增操作
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    
    }
    
    /**
     * 前置通知，在通知中给公共字段赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始填充公共字段");
        //获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //获取操作类型
        OperationType operationType = autoFill.value();
        //获取所有参数，实体类为第一个
        Object[] args = joinPoint.getArgs();
        if(args != null && args.length != 0){
            Object entity = args[0];
            //准备要赋值的数据
            LocalDateTime now = LocalDateTime.now();
            Long currentId = BaseContext.getCurrentId();
            
            if(operationType == OperationType.INSERT){
                //为4个公共字段赋值
                try {
                    //分别获取4个方法
                    Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                    //通过反射赋值
                    setCreateTime.invoke(entity, now);
                    setUpdateTime.invoke(entity, now);
                    setCreateUser.invoke(entity, currentId);
                    setUpdateUser.invoke(entity, currentId);
                }catch (Exception e) {
                    log.error("公共字段赋值错误：{}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }else if(operationType == OperationType.UPDATE){
                //仅赋值2个
                try {
                    //分别获取2个方法
                    Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                    //通过反射赋值
                    setUpdateTime.invoke(entity, now);
                    setUpdateUser.invoke(entity, currentId);
                }catch (Exception e) {
                    log.error("公共字段赋值错误：{}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
