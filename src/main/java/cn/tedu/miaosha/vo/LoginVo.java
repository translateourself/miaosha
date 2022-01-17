package cn.tedu.miaosha.vo;

import cn.tedu.miaosha.validator.IsMobile;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {
    @NotNull
    //自定义注解
    @IsMobile(required = true)
    private String mobile;
    @NotNull
    @Length(min=32)
    private String password;
}
