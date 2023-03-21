package com.macro.mall.portal.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.PromotionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PromotionPriceHandler implements PromotionHandler {

    @Override
    public void handlerPromotion(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, List<PromotionProduct> promotionProducts, PromotionProduct promotionProduct) {
        //单品促销
        for (OmsCartItem item : itemList) {
            CartPromotionItem cartPromotionItem = new CartPromotionItem();
            BeanUtils.copyProperties(item, cartPromotionItem);
            cartPromotionItem.setPromotionMessage("单品促销");
            //商品原价-促销价
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
            BigDecimal originalPrice = skuStock.getPrice();
            //单品促销使用原价
            cartPromotionItem.setPrice(originalPrice);
            cartPromotionItem.setReduceAmount(originalPrice.subtract(skuStock.getPromotionPrice()));
            cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
            cartPromotionItemList.add(cartPromotionItem);
        }
    }
}
