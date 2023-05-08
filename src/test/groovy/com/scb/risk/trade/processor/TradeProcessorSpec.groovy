package com.scb.risk.trade.processor

import com.scb.risk.trade.model.ProductData
import com.scb.risk.trade.model.TradeData
import com.scb.risk.trade.model.TradeRawData
import com.scb.risk.trade.staticdata.ReferenceData
import com.scb.risk.trade.validation.TradeValidator
import com.scb.risk.trade.validation.Validator
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class TradeProcessorSpec extends Specification {

    void "given trade data fed to trade processor, check if process and completes initial load"() {

        given:
            ReferenceData<ProductData> referenceData = Mock(ReferenceData) {
                _ * getProduct(_) >> new ProductData(data.id, staticdata)
            }
            Validator tradeDateValidator = TradeValidator.DATE_VALID

        and:
            TradeProcessor<TradeRawData, TradeData> processor = new BaseTradeProcessor(name, referenceData, tradeDateValidator)

        when:
            '''
                Process the data and sleep for a while and 
                check, validate for the enriched trade data
            '''
            processor.offer(data)

        and:
            Thread.sleep(10L)

        and:
            def tradeData = processor.pop()

        then:
            !processor.isDone()
            tradeData != null
            tradeData == [new TradeData('20121209', staticdata, 'EUR', 89.90D)]

        where:
            name                | staticdata        |   data
            'trade-processor'   | 'Euro Bond'       |   new TradeRawData('20121209', 1, 'EUR', 89.90D)
    }

    void "given trade raw data, check if the data is enriched and validated for trade data"() {
        given:
            ReferenceData<ProductData> referenceData = Mock(ReferenceData) {
                _ * getProduct(_) >> new ProductData(data.id, 'Euro Bond')
            }

        and:
            Validator tradeDateValidator = Mock(Validator) {
                _ * isValid(_) >> true
            }

        and:
            TradeProcessor<TradeRawData, TradeData> processor = new BaseTradeProcessor('trade-processor', referenceData, tradeDateValidator)

        when:
            TradeData enrichedData = processor.enrichTrades(data)

        then:
            enrichedData != null
            enrichedData == new TradeData('20121209', 'Euro Bond', 'EUR', 89.90D)

        where:
            isValid | data
            true    | new TradeRawData('20121209', 1, 'EUR', 89.90D)
    }

    void "when trade is validated, check if validator is called"() {
        given:
            ReferenceData<ProductData> referenceData = Mock(ReferenceData)
            Validator tradeDateValidator = Mock(Validator)

        and:
            TradeProcessor<TradeRawData, TradeData> processor = new BaseTradeProcessor('trade-processor', referenceData, tradeDateValidator)

        and:
            TradeData data = new TradeData('date', 'name', 'currency', 100D)
        when:
            processor.isValid(data)

        then:
            1 * tradeDateValidator.isValid(_)

    }

}
