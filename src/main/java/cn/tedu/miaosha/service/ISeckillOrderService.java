package cn.tedu.miaosha.service;

import cn.tedu.miaosha.pojo.SeckillOrder;
import cn.tedu.miaosha.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-18
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {


    Long getResult(User user, Long goodsId);
}
