package com.scb.risk.trade.validation;

import com.scb.risk.trade.model.TradeData;

public interface Validator<T extends TradeData> {
    public boolean isValid(T data);

}
