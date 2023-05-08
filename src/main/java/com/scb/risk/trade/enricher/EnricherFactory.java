package com.scb.risk.trade.enricher;

import com.scb.risk.trade.model.ProductData;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.processor.BaseTradeProcessor;
import com.scb.risk.trade.processor.TradeProcessor;
import com.scb.risk.trade.staticdata.ProductDataService;
import com.scb.risk.trade.staticdata.ReferenceData;
import com.scb.risk.trade.validation.TradeValidator;
import com.scb.risk.trade.validation.Validator;

public class EnricherFactory {

    public static Enricher<TradeData> createTradeEnricher() {
        ReferenceData<ProductData> referenceDataService = ProductDataService.getInstance();
        Validator<TradeData> validator = TradeValidator.DATE_VALID;
        TradeProcessor<TradeRawData, TradeData> processor = new BaseTradeProcessor("trade-processor", referenceDataService, validator);
        return new TradeEnricher("trade-enricher", processor);
    }
}
