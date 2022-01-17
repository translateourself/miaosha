package cn.tedu.miaosha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 首页
 * http://localhost:8080/login/toLogin
 *  用户： 13333333333  密码： 123456
 *
 *
 * 有问题可以添加  QQ:21815700  备注添加好友说明
 * 如果项目想马上启动，可以给我QQ发个小小的红包，
 * 我把我服务器，启动好的数据库，redis，mq都发送给你，
 * 在配置文件中填上ip就可以启动项目了。
 */
@SpringBootApplication
@MapperScan("cn.tedu.miaosha.mapper")
public class MiaoshaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiaoshaApplication.class, args);
        System.out.println("--------------------------------");
        System.out.println("------------启动成功-------------");
        System.out.println("--------------------------------");
    }

}
