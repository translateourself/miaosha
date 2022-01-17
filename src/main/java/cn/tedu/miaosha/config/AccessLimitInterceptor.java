package cn.tedu.miaosha.config;


import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.IUserService;
import cn.tedu.miaosha.utills.CookieUtil;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 自定义一个拦截器，用于实现接口限流
 * preHandle    执行之前的处理
 * postHandle   执行之后的处理
 * afterCompletion  处理完成之后要具体执行的方法
 */

@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断handler是否属于处理方法
        if(handler instanceof HandlerMethod){
            //获取当前用户
            User user = getUser(request,response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            //获取注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            //判断是否有这个注解
            if (accessLimit==null){
                return true;
            }
            //获取注解的属性
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin= accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin){
                if (user==null){
                    //返回错误信息
                    render(response,RespBeanEnum.SESSION_ERROR);
                    //拦截掉
                    return false;
                }
                key+=":"+user.getId();
                ValueOperations valueOperations = redisTemplate.opsForValue();
                Integer count = (Integer) valueOperations.get(key);
                //如果key为空设置值
                if (count==null){
                    valueOperations.set(key,1,second, TimeUnit.SECONDS);
                //小于限制次数，增加我们的值
                } else if (count < maxCount) {
                    valueOperations.increment(key);
                //超过我们的值，返回错误信息
                } else {
                    render(response,RespBeanEnum.ACCESS_LIMIT_REAHCED);
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * 构建对象(返回错误信息)
     * @param response
     * @param sessionError
     */
    private void render(HttpServletResponse response, RespBeanEnum sessionError) throws IOException {
        //设置编码UTF-8，返回类型json
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        //返回
        PrintWriter out = response.getWriter();
        RespBean bean = RespBean.error(sessionError);
        out.write(new ObjectMapper().writeValueAsString(bean));
        out.flush();
        out.close();
    }

    /**
     * 获取当前登录用户
     * @param request
     * @param response
     * @return
     */
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(ticket)){
            return null;
        }
        return userService.getUserByCookie(ticket,response,request);
    }
}
