package cn.tedu.miaosha.service.impl;

import cn.tedu.miaosha.mapper.SeckillOrderMapper;
import cn.tedu.miaosha.pojo.SeckillOrder;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.ISeckillOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-18
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 获取秒杀结果
     *
     * @param user
     * @param goodsId
     * @return orderId: 成功-1 失败 0  排队中
     */
    @Override
    public Long getResult(User user, Long goodsId) {
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder !=null){
            return seckillOrder.getId();
        }else if (redisTemplate.hasKey("isStockEmpty:"+goodsId)){
            return -1L;

        }else {
            return 0L;
        }
    }
}
