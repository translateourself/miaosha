package cn.tedu.miaosha.config;

import cn.tedu.miaosha.pojo.User;
import cn.tedu.miaosha.service.IUserService;
import cn.tedu.miaosha.utills.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义用户参数
 * @since 1.0.0
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
	@Autowired
	private IUserService userService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == User.class;
	}

	/**
	 * 获取到用户
	 * @param parameter
	 * @param mavContainer
	 * @param webRequest
	 * @param binderFactory
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
	                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//		String ticket = CookieUtil.getCookieValue(request, "userTicket");
//		if (StringUtils.isEmpty(ticket)){
//			return null;
//		}
//		return userService.getUserByCookie(ticket,response,request);
		return UserContext.getUser();
	}
}