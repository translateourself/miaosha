package cn.tedu.miaosha.controller;


import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.rabbitmq.MQSender;
import cn.tedu.miaosha.vo.RespBean;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wudenghao
 * @since 2021-12-17
 */
@Controller
@RequestMapping("/user")
@Accessors
public class UserController {
    @Autowired
    private MQSender mqSender;

    /**
     * 功能描述: 用户信息(测试)
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }
//
//    /**
//     *测试MQ
//     */
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("hello");
//    }
//
//    /**
//     *测试fanout模式
//     */
//    @RequestMapping("/fanout")
//    @ResponseBody
//    public void mq01(){
//        mqSender.send("hello");
//    }
//
//    @RequestMapping("/direct01")
//    @ResponseBody
//    public void mq02(){
//        mqSender.send01("hello");
//    }
//
//    @RequestMapping("/direct02")
//    @ResponseBody
//    public void mq03(){
//        mqSender.send02("hello");
//    }
//
//    @RequestMapping("/topic01")
//    @ResponseBody
//    public void mq04(){
//        mqSender.send03("hello");
//    }
//
//    @RequestMapping("/topic02")
//    @ResponseBody
//    public void mq05(){
//        mqSender.send04("hello");
//    }
//
//    @RequestMapping("/header01")
//    @ResponseBody
//    public void mq06(){
//        mqSender.send06("hello01");
//    }
//    @RequestMapping("/header02")
//    @ResponseBody
//    public void mq07(){
//        mqSender.send07("hello02");
//    }
}


