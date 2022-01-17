package cn.tedu.miaosha.service.impl;

import cn.tedu.miaosha.exception.GlobalException;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.mapper.UserMapper;
import cn.tedu.miaosha.service.IUserService;
import cn.tedu.miaosha.utills.CookieUtil;
import cn.tedu.miaosha.utills.MD5Util;
import cn.tedu.miaosha.utills.UUIDUtil;
import cn.tedu.miaosha.utills.ValidatorUtil;
import cn.tedu.miaosha.vo.LoginVo;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-17
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //参数校验，自定义了注解去校验
        /*//判断用户名或者手机号码是否为空
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        if(!ValidatorUtil.isMobile(mobile)){
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }*/
        //判断用户是否存在
        User user = userMapper.selectById(mobile);
        if (user==null){
            //return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //再次MD5加密和数据库进行验证密码
        if (!MD5Util.formPassToDBPass(password,user.getSlat()).equals(user.getPassword())){
            // return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
        //存入redis
        redisTemplate.opsForValue().set("user:"+ticket,user);
        //设置返回的session,
        //request.getSession().setAttribute(ticket, user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket,HttpServletResponse response,HttpServletRequest request) {
        if (StringUtils.isEmpty(userTicket)){
            return null;
        }
        //从redis获取用户对象
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if (user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }

    /**
     * 更新密码
     * @param userTicket
     * @param password
     * @param response
     * @param request
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket, String password,HttpServletResponse response,HttpServletRequest request) {
        User user = getUserByCookie(userTicket, response, request);
        if (user==null){
            throw new  GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        //设置密码
        user.setPassword(MD5Util.formPassToDBPass(password,user.getPassword()));
        int result = userMapper.updateById(user);
        if (1==result){
            redisTemplate.delete("user"+userTicket);
            return RespBean.success();
        }
        //失败返回，实际业务就是提示用户，修改失败
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
