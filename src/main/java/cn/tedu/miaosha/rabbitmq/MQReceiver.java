package cn.tedu.miaosha.rabbitmq;

import cn.tedu.miaosha.pojo.SeckillMessage;
import cn.tedu.miaosha.pojo.SeckillOrder;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.IGoodsService;
import cn.tedu.miaosha.service.IOrderService;
import cn.tedu.miaosha.utills.JsonUtil;
import cn.tedu.miaosha.vo.GoodsVo;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;
    /**
     * 接收到消息进行下单操作
     */
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("收到消息："+message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        User user = seckillMessage.getUser();
        Long goodsId = seckillMessage.getGoodId();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存是否充足
        if (goodsVo.getStockCount()<1){
            return;
        }
        //判断重复抢购"
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder!=null){
            return ;
        }
        //下单操作
        orderService.seckill(user,goodsVo);
        //处理消息之前给客户端返回的是正在下单中，这里下单完成之后还有返回给客户端，通过轮询的方式返回
    }

//    @RabbitListener(queues = "queue")
//    public void receive(Object msg){
//        log.info("接收消息"+msg);
//    }
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("queue01接收消息"+msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("queue02接收消息"+msg);
//    }
//
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg){
//        log.info("queue01接收消息"+msg);
//    }
//
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg){
//        log.info("queue02接收消息"+msg);
//    }
//
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg){
//        log.info("queue02接收消息"+msg);
//    }
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg){
//        log.info("queue02接收消息"+msg);
//    }
//    @RabbitListener(queues = "queue_header01")
//    public void receive07(Message msg){
//        log.info("queue01接收message对象"+msg);
//        log.info("queue01接收message消息"+ new String(msg.getBody()));
//
//    }
//    @RabbitListener(queues = "queue_header02")
//    public void receive08(Message msg){
//        log.info("queue02接收message对象"+msg);
//        log.info("queue02接收message消息"+ new String(msg.getBody()));
//    }

}
