package cn.tedu.miaosha.config;

import cn.tedu.miaosha.pojo.User;

/**
 * 储存用户信息
 */
public class UserContext {

    /**
     * 解决每个线程绑定到自己的值，这个值只有当然线程可以见，
     * 可以理解为这个线程存储自己私有的信息
     */
    private static ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }
}
