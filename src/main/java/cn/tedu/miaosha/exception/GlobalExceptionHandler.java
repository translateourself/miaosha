package cn.tedu.miaosha.exception;

import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理类
 *
 * 全局异常处理的两种方案：
 * 1.实现HandlerExceptionResolver接口
 *    可以处理所有异常，未进入和已进入拦截器的异常
 * 2.@ControllerAdvice+@ExceptionHandler
 *    进入控制器的异常
 *    可以定义多个异常信息，返回不同的信息
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e) {
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            //如果我们的异常属于全局异常，抛出全局异常处理方案
            return RespBean.error(ex.getRespBeanEnum());
        } else if (e instanceof BindException) {
            //绑定异常
            BindException ex = (BindException) e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常：" + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        //返回ResponseBody
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
