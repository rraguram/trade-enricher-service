package com.scb.risk.trade.staticdata;

import com.scb.risk.trade.model.ProductData;

public interface ReferenceData <T extends ProductData> {

    public T getProduct(Long productId);
}
