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
 * 公共字段自动填充切面
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 拦截mapper包中带有AutoFill注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 在执行目标方法前填充公共字段
     *
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            if (operationType == OperationType.INSERT) {
                invokeSetter(entity, AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class, now);
                invokeSetter(entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
                invokeSetter(entity, AutoFillConstant.SET_CREATE_USER, Long.class, currentId);
                invokeSetter(entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);
            } else if (operationType == OperationType.UPDATE) {
                invokeSetter(entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
                invokeSetter(entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);
            }
        } catch (Exception ex) {
            throw new RuntimeException("公共字段自动填充失败", ex);
        }
    }

    /**
     * 通过反射调用实体类的setter方法
     *
     * @param entity
     * @param methodName
     * @param parameterType
     * @param value
     * @throws Exception
     */
    private void invokeSetter(Object entity, String methodName, Class<?> parameterType, Object value) throws Exception {
        Method setter = entity.getClass().getDeclaredMethod(methodName, parameterType);
        setter.invoke(entity, value);
    }

}
