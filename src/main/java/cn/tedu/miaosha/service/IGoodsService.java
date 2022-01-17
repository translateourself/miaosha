package cn.tedu.miaosha.service;

import cn.tedu.miaosha.pojo.Goods;
import cn.tedu.miaosha.vo.GoodsVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author wudenghao
 * @since 2021-12-18
 */
public interface IGoodsService extends IService<Goods> {
    /**
     *商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
