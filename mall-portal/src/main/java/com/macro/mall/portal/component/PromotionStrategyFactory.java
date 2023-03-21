package com.macro.mall.portal.component;

import com.macro.mall.portal.service.PromotionHandler;
import com.macro.mall.portal.service.impl.MarkdownPromotionHandler;
import com.macro.mall.portal.service.impl.NoPromotionHandler;
import com.macro.mall.portal.service.impl.PromotionPriceHandler;
import com.macro.mall.portal.service.impl.StairwayPromotionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PromotionStrategyFactory {
    @Autowired
    private NoPromotionHandler noPromotionHandler;
    @Autowired
    private StairwayPromotionHandler stairwayPromotionHandler;
    @Autowired
    private PromotionPriceHandler promotionPriceHandler;
    @Autowired
    private MarkdownPromotionHandler markdownPromotionHandler;

    public PromotionHandler getHandlerByPromotionType(Integer type) {
        if (Objects.isNull(type)) {
            return noPromotionHandler;
        }
        switch (type) {
            case 1:
                return promotionPriceHandler;
            case 3:
                return stairwayPromotionHandler;
            case 4:
                return markdownPromotionHandler;
            default:
                throw new UnsupportedOperationException("不支持促销策略");
        }
    }
}
