package cn.tedu.miaosha;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MiaoshaApplicationTests {


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisScript<Boolean> redisScript;
    @Test
    void testLock01() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //站位，如果K不存在才可以设置成功
        Boolean aBoolean = valueOperations.setIfAbsent("k1", "v1");
        //如果站位成功，进行正常操作
        if (aBoolean){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name="+name);
            //模拟抛出异常，如果中途抛了异常就不能删除锁
            Integer.parseInt("xxx");
            //操作介绍，删除锁
            redisTemplate.delete("k1");
        }else {
            System.out.println("有线程在使用，请稍微再试");
        }
    }

    /**
     * 改进上面的操作，上锁之后设置时间自动删除锁
     */
    @Test
    void testLock02() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //站位，如果K不存在才可以设置成功,如果抛出异常，5秒自动删除锁
        Boolean aBoolean = valueOperations.setIfAbsent("k1", "v1",5, TimeUnit.SECONDS);
        //如果站位成功，进行正常操作
        if (aBoolean){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name="+name);
            Integer.parseInt("xxx");
            //操作介绍，删除锁
            redisTemplate.delete("k1");
        }else {
            System.out.println("有线程在使用，请稍微再试");
        }
    }

    /**
     * 改进上面，如果执行时间超过设置删除锁的时间，那么就会出现，锁紊乱，当前的锁会删除下一个线程的锁
     * 改进：我们在删除锁的时候，进行比对，我删除的锁是不是我自己的锁
     * 释放锁有三个步骤，获取锁、比较锁、释放锁
     * 但是这个三个操作不具有原子性，我们可以写lua脚本，来保证原子性
     * lua可以写在java程序 优：易于修改  缺:需要网络传输，占用资源，
     *     可以写在redis服务端  优：不占用网络资源  缺：不易于修改
     */
    @Test
    void testLock03() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //站位，如果K不存在才可以设置成功,如果抛出异常，5秒自动删除锁
        //对我们valeue设置随机值
        String value = UUID.randomUUID().toString();
        Boolean aBoolean = valueOperations.setIfAbsent("k1", value,5, TimeUnit.SECONDS);
        //如果站位成功，进行正常操作
        if (aBoolean){
            valueOperations.set("name", "xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name="+name);
            System.out.println("k1");
            //执行redis脚本,删除锁
            Boolean result = (Boolean) redisTemplate.execute(redisScript, Collections.singletonList("k1"), value);
            System.out.println(result);
        }else {
            System.out.println("有线程在使用，请稍微再试");
        }
    }

}
