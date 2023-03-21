package com.macro.mall.portal.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsProductLadder;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.PromotionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StairwayPromotionHandler implements PromotionHandler {

    @Override
    public void handlerPromotion(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, List<PromotionProduct> promotionProducts, PromotionProduct promotionProduct) {
        //打折优惠
        int count = getCartItemCount(itemList);
        PmsProductLadder ladder = getProductLadder(count, promotionProduct.getProductLadderList());
        if (ladder != null) {
            for (OmsCartItem item : itemList) {
                CartPromotionItem cartPromotionItem = new CartPromotionItem();
                BeanUtils.copyProperties(item, cartPromotionItem);
                String message = getLadderPromotionMessage(ladder);
                cartPromotionItem.setPromotionMessage(message);
                //商品原价-折扣*商品原价
                PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
                BigDecimal originalPrice = skuStock.getPrice();
                BigDecimal reduceAmount = originalPrice.subtract(ladder.getDiscount().multiply(originalPrice));
                cartPromotionItem.setReduceAmount(reduceAmount);
                cartPromotionItem.setRealStock(skuStock.getStock() - skuStock.getLockStock());
                cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                cartPromotionItemList.add(cartPromotionItem);
            }
        } else {
            handleNoReduce(cartPromotionItemList, itemList, promotionProduct);
        }
    }


    /**
     * 获取打折优惠的促销信息
     */
    private String getLadderPromotionMessage(PmsProductLadder ladder) {
        return "打折优惠：" + "满" + ladder.getCount() + "件，" + "打" + ladder.getDiscount().multiply(new BigDecimal(10)) + "折";
    }

    /**
     * 根据购买商品数量获取满足条件的打折优惠策略
     */
    private PmsProductLadder getProductLadder(int count, List<PmsProductLadder> productLadderList) {
        //按数量从大到小排序
        productLadderList.sort((o1, o2) -> o2.getCount() - o1.getCount());
        for (PmsProductLadder productLadder : productLadderList) {
            if (count >= productLadder.getCount()) {
                return productLadder;
            }
        }
        return null;
    }

    /**
     * 获取购物车中指定商品的数量
     */
    private int getCartItemCount(List<OmsCartItem> itemList) {
        int count = 0;
        for (OmsCartItem item : itemList) {
            count += item.getQuantity();
        }
        return count;
    }
}
