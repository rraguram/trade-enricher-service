package com.scb.risk.trade.enricher

import com.scb.risk.trade.model.TradeData
import com.scb.risk.trade.processor.BaseTradeProcessor
import com.scb.risk.trade.processor.TradeProcessor
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

@Unroll
class EnricherServiceSpec extends Specification {

    void "given raw trade data, #data, process to enrich the trades and validate it, #expectedData"() {

        given:
            TradeProcessor processor = Mock(TradeProcessor) {
                1 * offer(_)
                1 * count() >> 1
                1 * pop()  >> expectedData
            }
            TradeEnricher enricher = new TradeEnricher(name, 10L, processor)

        and:
            InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))

        when:
            Iterable<? extends TradeData> tradeData = enricher.processStream(inputStream)

        then:
            tradeData != null
            tradeData == expectedData

        where:
            name                | poolSize  | data                      || expectedData
            'trade-enricher'    | 1         | '20160101,1,EUR,10.0'     || ['20160101,Bond1,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,2,EUR,10.0'     || ['20160101,Bond2,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,3,EUR,10.0'     || ['20160101,Bond3,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,4,EUR,10.0'     || ['20160101,Bond4,EUR,10.0']
    }

    void "when trades get processed, check if processed time before provided elapsed time"() {

        given:
            TradeProcessor processor = Mock(TradeProcessor) {
                1 * offer(_)
                _ * count() >> 0
                _ * pop()  >> [data]
            }
            TradeEnricher enricher = new TradeEnricher(name, 10L, processor)

        and:
            InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))

        when:
            Iterable<? extends TradeData> tradeData = enricher.processStream(inputStream)

        then:
            thrown RuntimeException

        where:
            name                | poolSize  | data                      | elaspedTime
            'trade-enricher'    | 1         | '20160101,1,EUR,10.0'     | 0

    }

}
