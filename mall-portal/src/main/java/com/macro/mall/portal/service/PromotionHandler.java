package com.macro.mall.portal.service;

import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.PromotionProduct;
import com.macro.mall.portal.service.impl.NoPromotionHandler;

import java.util.Collections;
import java.util.List;

/**
 * todo 接口抽象成策略模式，但是目前接口违反了接口隔离原则，还有就是这一部分应该还有一部分重复的代码，看看还能不能继续优化下去
 */
public interface PromotionHandler {

    /**
     * 处理促销
     *
     * @param cartPromotionItemList
     * @param itemList
     * @param promotionProduct
     * @param product
     * @return
     */
    void handlerPromotion(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, List<PromotionProduct> promotionProduct, PromotionProduct product);

    default void handleNoReduce(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList, PromotionProduct promotionProduct) {
        new NoPromotionHandler().handlerPromotion(cartPromotionItemList, itemList, Collections.singletonList(promotionProduct), promotionProduct);
    }

    /**
     * 获取商品的库存对象
     */
    default PmsSkuStock getOriginalPrice(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }
}
