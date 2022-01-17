package cn.tedu.miaosha.service.impl;

import cn.tedu.miaosha.exception.GlobalException;
import cn.tedu.miaosha.mapper.OrderMapper;
import cn.tedu.miaosha.pojo.Order;
import cn.tedu.miaosha.pojo.SeckillGoods;
import cn.tedu.miaosha.pojo.SeckillOrder;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.IGoodsService;
import cn.tedu.miaosha.service.IOrderService;
import cn.tedu.miaosha.service.ISeckillGoodsService;
import cn.tedu.miaosha.service.ISeckillOrderService;
import cn.tedu.miaosha.utills.MD5Util;
import cn.tedu.miaosha.utills.UUIDUtil;
import cn.tedu.miaosha.vo.GoodsVo;
import cn.tedu.miaosha.vo.OrderDetailVo;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-18
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀业务,
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断用户是否已经进行购买
        SeckillOrder seckillOrder1 = (SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goods.getId());
        if (seckillOrder1!=null){
            throw new GlobalException(RespBeanEnum.REPEATE_ERROR);
        }
        //秒杀商品，查出商品库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id",goods.getId()));
        //减少库存
        //seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        //更新库存
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count-1")
                .eq("goods_id", goods.getId())
                .gt("stock_count", 0));
//        if (!result){
//            return null;
//        }
        if (seckillGoods.getStockCount()<1){
            //判断是否还有库存
            valueOperations.set("isStockEmpty:"+goods.getId(),"0");
            return null;
        }
        //seckillGoodsService.updateById(seckillGoods);
        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order"+user.getId()+":"+goods.getId(),seckillOrder);
        return order;
    }

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId==null){
            throw  new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        //根据订单id获取订单信息
        Order order = orderMapper.selectById(orderId);
        //根据订单id获取商品信息
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        //在我们原有的接口中生成一个加密的随机UUID
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        //接口地址要保存起来，后续要进行对比，存入redis
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId,str,60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public Boolean checkPath(String path,User user, Long goodsId) {
        if (user==null ||goodsId<0 || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return redisPath.equals(path);
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public boolean checkCaptche(User user, Long goodsId, String captcha) {
        if (StringUtils.isEmpty(captcha) || user==null ||goodsId<0){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:"+user.getId()+":"+goodsId);
        System.out.println("收到输入的验证码结果"+redisCaptcha);
        return captcha.equals(redisCaptcha);
    }
}
