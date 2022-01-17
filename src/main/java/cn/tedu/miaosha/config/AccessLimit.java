package cn.tedu.miaosha.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，实现通用接口的限流，采用计数器算法实现
 * 基于拦截器实现
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    //次数
    int second();
    //统计时长
    int maxCount();
    //检测用户是否登录
    boolean needLogin() default true;
}
