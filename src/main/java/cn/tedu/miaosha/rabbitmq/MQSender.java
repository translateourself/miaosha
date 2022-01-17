package cn.tedu.miaosha.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQSender {
    @Autowired
    RabbitTemplate rabbitTemplate;

/*
    public void send(Object msg) {
        log.info("发送消息" + msg);
        rabbitTemplate.convertAndSend("FanoutExchange", "", msg);
    }

    public void send01(Object msg) {
        log.info("发送red消息" + msg);
        rabbitTemplate.convertAndSend("DirectExchange", "queue.red", msg);
    }

    public void send02(Object msg) {
        log.info("发送green消息" + msg);
        rabbitTemplate.convertAndSend("DirectExchange", "queue.green", msg);
    }

    public void send03(Object msg) {
        log.info("发送消息queue01接收" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "queue.green", msg);
    }

    public void send04(Object msg) {
        log.info("发送消息,被两个接收" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "abc.queue.green.abc", msg);
    }

    public void send06(String msg) {
        log.info("发送消息,被两个接收" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color","red");
        properties.setHeader("speed","fast");
        Message message = new Message(msg.getBytes(),properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }
    public void send07(String msg) {
        log.info("发送消息,被queue01接收" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color","red");
        properties.setHeader("speed","normal");
        Message message = new Message(msg.getBytes(),properties);
        rabbitTemplate.convertAndSend("headersExchange", "", message);
    }*/

    /**
     * 发送秒杀信息
     * @param message
     */
    public void sendSeckillMessage(String message){
        log.info("发送消息：" + message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }

}
