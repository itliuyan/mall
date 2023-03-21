package com.macro.mall.portal.service.impl;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsProductFullReduction;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.PromotionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class MarkdownPromotionHandler implements PromotionHandler {


    @Override
    public void handlerPromotion(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, List<PromotionProduct> promotionProductList, PromotionProduct promotionProduct) {
        //满减
        BigDecimal totalAmount = getCartItemAmount(itemList, promotionProductList);
        PmsProductFullReduction fullReduction = getProductFullReduction(totalAmount, promotionProduct.getProductFullReductionList());
        if (fullReduction != null) {
            for (OmsCartItem item : itemList) {
                CartPromotionItem cartPromotionItem = new CartPromotionItem();
                BeanUtils.copyProperties(item, cartPromotionItem);
                String message = getFullReductionPromotionMessage(fullReduction);
                cartPromotionItem.setPromotionMessage(message);
                //(商品原价/总价)*满减金额
                PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
                BigDecimal originalPrice = skuStock.getPrice();
                BigDecimal reduceAmount = originalPrice.divide(totalAmount, RoundingMode.HALF_EVEN).multiply(fullReduction.getReducePrice());
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
     * 根据商品id获取商品的促销信息
     */
    private PromotionProduct getPromotionProductById(Long productId, List<PromotionProduct> promotionProductList) {
        for (PromotionProduct promotionProduct : promotionProductList) {
            if (productId.equals(promotionProduct.getId())) {
                return promotionProduct;
            }
        }
        return null;
    }

    /**
     * 获取购物车中指定商品的总价
     */
    private BigDecimal getCartItemAmount(List<OmsCartItem> itemList, List<PromotionProduct> promotionProductList) {
        BigDecimal amount = new BigDecimal(0);
        for (OmsCartItem item : itemList) {
            //计算出商品原价
            PromotionProduct promotionProduct = getPromotionProductById(item.getProductId(), promotionProductList);
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
            amount = amount.add(skuStock.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return amount;
    }

    /**
     * 获取满减促销消息
     */
    private String getFullReductionPromotionMessage(PmsProductFullReduction fullReduction) {
        String sb = "满减优惠：" +
                "满" +
                fullReduction.getFullPrice() +
                "元，" +
                "减" +
                fullReduction.getReducePrice() +
                "元";
        return sb;
    }

    private PmsProductFullReduction getProductFullReduction(BigDecimal totalAmount, List<PmsProductFullReduction> fullReductionList) {
        //按条件从高到低排序
        fullReductionList.sort(new Comparator<PmsProductFullReduction>() {
            @Override
            public int compare(PmsProductFullReduction o1, PmsProductFullReduction o2) {
                return o2.getFullPrice().subtract(o1.getFullPrice()).intValue();
            }
        });
        for (PmsProductFullReduction fullReduction : fullReductionList) {
            if (totalAmount.subtract(fullReduction.getFullPrice()).intValue() >= 0) {
                return fullReduction;
            }
        }
        return null;
    }
}
