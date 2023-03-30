package com.scb.risk.trade.enricher;

import com.google.common.base.Preconditions;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.processor.BaseTradeProcessor;
import com.scb.risk.trade.processor.TradeProcessor;

public class EnricherFactory {

    public static TradeEnricher create(String name, int poolSize) {
        Preconditions.checkNotNull(name, "name is empty/null");
        Preconditions.checkArgument(poolSize > 0, "pool size must be positive");

        TradeProcessor<TradeRawData, TradeData> processor = new BaseTradeProcessor("trade-processor-"+name, poolSize);
        return new TradeEnricher("trade-enricher-"+name, processor);
    }
}
