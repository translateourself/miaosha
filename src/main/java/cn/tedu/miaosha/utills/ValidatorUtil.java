package cn.tedu.miaosha.utills;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码参数校验工具类
 */
public class ValidatorUtil {

	private static final Pattern mobile_pattern = Pattern.compile("[1]([3-9])[0-9]{9}$");
	//判断是否为空，在进行正则表达式进行匹配
	public static boolean isMobile(String mobile){
		if (StringUtils.isEmpty(mobile)){
			return false;
		}
		Matcher matcher = mobile_pattern.matcher(mobile);
		return matcher.matches();
	}

}