package cn.tedu.miaosha.utills;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MD5Util {
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
}

    private static final String salt = "1a2b3c4d";

    /**
     * 第一加密和前端加密一样
     * @param inputPass
     * @return
     */
    public static String inputPassToFromPass(String inputPass){
        String str = "" +salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 加密到数据库
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass,String salt){
        String str = "" +salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    /**
     * 接收前端加密，并进行第二次加密，生成数据库密码
     * @param inputPass
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String inputPass,String salt){
        String fromPass = inputPassToFromPass(inputPass);
        String dbPass = formPassToDBPass(fromPass, salt);
        return dbPass;
    }


    public static void main(String[] args) {
        // d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToFromPass("123456"));
        System.out.println(formPassToDBPass("d3b1294a61a07da9b49b6e22b2cbd7f9","1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }
}
