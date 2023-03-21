package com.macro.mall.portal.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.PromotionHandler;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;

@Server
public class NoPromotionHandler implements PromotionHandler {

    @Override
    public void handlerPromotion(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, List<PromotionProduct> promotionProducts, PromotionProduct promotionProduct) {
        for (OmsCartItem item : itemList) {
            CartPromotionItem cartPromotionItem = new CartPromotionItem();
            BeanUtils.copyProperties(item, cartPromotionItem);
            cartPromotionItem.setPromotionMessage("无优惠");
            cartPromotionItem.setReduceAmount(new BigDecimal(0));
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
            if (skuStock != null) {
                cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
            }
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
            cartPromotionItemList.add(cartPromotionItem);
        }
    }
}
