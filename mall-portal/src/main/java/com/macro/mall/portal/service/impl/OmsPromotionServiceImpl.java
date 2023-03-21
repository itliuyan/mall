package com.macro.mall.portal.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.macro.mall.model.*;
import com.macro.mall.portal.component.PromotionStrategyFactory;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.OmsPromotionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by macro on 2018/8/27.
 * 促销管理Service实现类
 */
@Service
public class OmsPromotionServiceImpl implements OmsPromotionService {
    @Autowired
    private PortalProductDao portalProductDao;
    @Resource
    private PromotionStrategyFactory promotionStrategyFactory;

    @Override
    public List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList) {
        //1.先根据productId对CartItem进行分组，以spu为单位进行计算优惠
        Map<Long, List<OmsCartItem>> productCartMap = groupCartItemBySpu(cartItemList);
        //2.查询所有商品的优惠相关信息
        List<PromotionProduct> promotionProductList = getPromotionProductList(cartItemList);
        Map<Long, PromotionProduct> promotionProductMap = getPromotionProductMap(promotionProductList);
        //3.根据商品促销类型计算商品促销优惠价格
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        for (Map.Entry<Long, List<OmsCartItem>> entry : productCartMap.entrySet()) {
            Long productId = entry.getKey();
            PromotionProduct promotionProduct = promotionProductMap.get(productId);
            List<OmsCartItem> itemList = entry.getValue();
            if (Objects.isNull(promotionProduct)) {
                List<CartPromotionItem> cartPromotionItems = BeanUtil.copyToList(itemList, CartPromotionItem.class);
                assert cartPromotionItems != null;
                cartPromotionItemList.addAll(cartPromotionItems);
            } else {
                promotionStrategyFactory.getHandlerByPromotionType(promotionProduct.getPromotionType())
                        .handlerPromotion(cartPromotionItemList, itemList, promotionProductList, promotionProduct);
            }

        }
        return cartPromotionItemList;
    }

    /**
     * 获取商品促销分组
     *
     * @param promotionProductList
     * @return
     */
    private Map<Long, PromotionProduct> getPromotionProductMap(List<PromotionProduct> promotionProductList) {
        return promotionProductList.stream().collect(Collectors.toMap(PmsProduct::getId, Function.identity()));
    }

    /**
     * 查询所有商品的优惠相关信息
     */
    private List<PromotionProduct> getPromotionProductList(List<OmsCartItem> cartItemList) {
        return portalProductDao.getPromotionProductList(cartItemList.stream().map(OmsCartItem::getProductId).collect(Collectors.toList()));
    }

    /**
     * 以spu为单位对购物车中商品进行分组
     */
    private Map<Long, List<OmsCartItem>> groupCartItemBySpu(List<OmsCartItem> cartItemList) {
        return cartItemList.stream().collect(Collectors.groupingBy(OmsCartItem::getProductId));
    }
}
