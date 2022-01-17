package cn.tedu.miaosha.controller;


import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.IOrderService;
import cn.tedu.miaosha.vo.OrderDetailVo;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-18
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;
    /**
     * 订单详情
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user,Long orderId){
        if (user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail= orderService.detail(orderId);
        return RespBean.success(detail);
    }

}
