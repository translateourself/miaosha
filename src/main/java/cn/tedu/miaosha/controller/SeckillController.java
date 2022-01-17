package cn.tedu.miaosha.controller;

import cn.tedu.miaosha.config.AccessLimit;
import cn.tedu.miaosha.exception.GlobalException;
import cn.tedu.miaosha.pojo.Order;
import cn.tedu.miaosha.pojo.SeckillMessage;
import cn.tedu.miaosha.pojo.SeckillOrder;
import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.rabbitmq.MQSender;
import cn.tedu.miaosha.service.IGoodsService;
import cn.tedu.miaosha.service.IOrderService;
import cn.tedu.miaosha.service.ISeckillOrderService;
import cn.tedu.miaosha.utills.JsonUtil;
import cn.tedu.miaosha.vo.GoodsVo;
import cn.tedu.miaosha.vo.RespBean;
import cn.tedu.miaosha.vo.RespBeanEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import com.wf.captcha.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping(value = "/seckill",method = RequestMethod.POST)
public class SeckillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> redisScript;

    private Map<Long,Boolean> EmptyStockMap = new HashMap<>();

    /**
     * 秒杀
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("doSeckill2")
    public String doSeckill2(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        //查询商品
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "seckillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));
        if (seckillOrder!=null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "seckillFail";
        }
        //可以秒杀
        Order order = orderService.seckill(user,goods);
        model.addAttribute("oder",order);
        model.addAttribute("goods",goods);
        return "orderDetail";
    }

    /**
     * 秒杀
     * @param path
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

       /* //查询商品查数据库
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存从数据库中拿数据
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复抢购数据库中取数据
        //SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                //.eq("user_id", user.getId())
                //.eq("goods_id", goodsId));

        //从redis里边获取用户是否抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goods.getId());
        if (seckillOrder!=null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //可以秒杀
        Order order = orderService.seckill(user,goods);
        return RespBean.success(order);
        */

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //校验我们的路径是否正确,校验成功往下执行，失败返回错误
        Boolean check = orderService.checkPath(path,user, goodsId);
        if (!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //1.判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order" + user.getId() + ":" + goodsId);
        if (seckillOrder!=null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //2.判断redis，预减库存
        //redis递减操作，是原子性操作
        //内存标记，减少redis的访问
        if (EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
        //Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock<0){
            EmptyStockMap.put(goodsId, true);
            //把库存-1变为0 使数据库好看点。
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user,goodsId);
        //3.发送消息到消息队列
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        //返回前端，前端处理，正在排队中，后端在消费
        return RespBean.success(0);
    }

    /**
     * 获取秒杀地址,先进行验证码校验
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if (user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        /**
         * 接口限流，主流的三种算法： 计数器算法，漏桶算法、令铜牌算法
         * 这里我们采用计数器算法实现我们的接口算法，使用redis实现，但是这个计数器算法有2个缺陷
         *      1.临界值问题
         *      2.资源浪费问题
         * 漏桶算法：  (保护他人)
         * 缺点：请求来的快、大，容易造成桶装满，服务器撑爆。一般用队列机制实现的
         * 令铜牌算法： 漏桶的改进     (保护自己)
         * 从桶里拿令牌，看有没有拿到令牌，大量请求过来可以应付，交给目标服务器处理
         */
        //判断验证码是否正确
        boolean check =  orderService.checkCaptche(user,goodsId,captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        //获取秒杀地址
        String str = orderService.createPath(user,goodsId);
        return RespBean.success(str);
    }

    /**
     * 生成验证码
     * 工具类：https://gitee.com/dp9212/EasyCaptcha?_from=gitee_search
     * @param user
     * @param goodsId
     * @param response
     */
    @RequestMapping(value = "/captcha",method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if (user==null ||goodsId<0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //生成验证码---算术类型， 放入redis中
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,captcha.text(),
                300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败");
        }
    }

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return orderId: 成功-1 失败 0  排队中
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId =  seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 初始化之后可以执行的一些方法
     * 系统初始化，把商品库存数量加载到redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //发现我们的库存
        List<GoodsVo> list = goodsService.findGoodsVo();
        //判断库存是否为空
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        //不为空，把库存全部存入redis中
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(),false);
        }
        );
    }
}
