package cn.tedu.miaosha.vo;

import cn.tedu.miaosha.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单详情返回对象
 * @author wudenghao
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {

	private Order order;

	private GoodsVo goodsVo;
}
