package cn.tedu.miaosha.vo;

import cn.tedu.miaosha.utills.ValidatorUtil;
import cn.tedu.miaosha.validator.IsMobile;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号码校验规则，要实现ConstraintValidator
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

	//是否是必填，默认false
	private boolean required = false;

	/**
	 *初始化，获取到获取到的值
	 * @param constraintAnnotation
	 */
	@Override
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	/**
	 *
	 * @param value
	 * @param context
	 * @return
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (required){
			return ValidatorUtil.isMobile(value);
		}else {
			if (StringUtils.isEmpty(value)){
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}
}