package cn.tedu.miaosha.service;

import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.vo.LoginVo;
import cn.tedu.miaosha.vo.RespBean;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  服务类
 */
public interface IUserService extends IService<User> {
    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);


    /**
     * 根据cookie获取用户
     * @param userTicket
     * @return
     */
    User getUserByCookie(String userTicket,HttpServletResponse response,HttpServletRequest request);

    /**
     * 更新密码
     * @param userTicket
     * @param password
     * @param response
     * @param request
     * @return
     */
    RespBean updatePassword(String userTicket,String password,HttpServletResponse response,HttpServletRequest request);
}
