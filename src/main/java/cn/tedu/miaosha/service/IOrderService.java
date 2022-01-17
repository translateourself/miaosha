package cn.tedu.miaosha.service;

import cn.tedu.miaosha.pojo.Order;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.vo.GoodsVo;
import cn.tedu.miaosha.vo.OrderDetailVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-18
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    Order seckill(User user, GoodsVo goods);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(User user, Long goodsId);

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    Boolean checkPath(String path,User user, Long goodsId);

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    boolean checkCaptche(User user, Long goodsId, String captcha);
}
