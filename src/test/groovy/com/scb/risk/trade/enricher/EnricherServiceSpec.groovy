package com.scb.risk.trade.enricher

import com.scb.risk.trade.model.TradeData
import com.scb.risk.trade.processor.TradeProcessor
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

@Unroll
class EnricherServiceSpec extends Specification {

    void "given raw trade data, #data, process to enrich the trades and validate it, #expectedData"() {

        given:
            TradeProcessor processor = Mock(TradeProcessor) {
                _ * offer(_)
                _ * count() >> 1
                _ * pop()  >> expectedData
            }
            TradeEnricher enricher = new TradeEnricher(name, processor)

        and:
            InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))

        when:
        '''
            Process the input stream 
        '''
            enricher.processStream(inputStream)

        and:
        '''
            Delay to give the enricher to process the trade data
        '''
            Thread.sleep(10L)
        and:
            Set<? extends TradeData> tradeData = enricher.getData()

        then:
        '''
            Validate for the trade data
        '''
            tradeData != null
            tradeData.first() == expectedData.first()

        where:
            name                | poolSize  | data                      || expectedData
            'trade-enricher'    | 1         | '20160101,1,EUR,10.0'     || ['20160101,Bond1,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,2,EUR,10.0'     || ['20160101,Bond2,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,3,EUR,10.0'     || ['20160101,Bond3,EUR,10.0']
            'trade-enricher'    | 1         | '20160101,4,EUR,10.0'     || ['20160101,Bond4,EUR,10.0']
    }

    void "when trade enricher receives input stream, check if processor is invoked to process trades"() {

        given:

            TradeProcessor processor = Mock(TradeProcessor) {
                _ * count() >> 0
                _ * pop()  >> [data]
            }

        and:
            TradeEnricher enricher = new TradeEnricher(name, processor)

        and:
            InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))

        when:
            enricher.processStream(inputStream)

        then:
            '''
                Check processor is invoked to process the trade data
            '''
            1 * processor.offer(_)

        and:
            '''
                Once load is completed, check if processor for initial load done.
            '''
            1 * processor.initialLoadDone()


        where:
            name                |  data
            'trade-enricher'    |  '20160101,1,EUR,10.0'
    }


    void "when trade enricher closed, validate if enricher processes the data and reset flags on close"() {

        given:
            TradeProcessor processor = Mock(TradeProcessor) {
                _ * pop()  >> [data]
                _ * isDone() >> true
            }

            TradeEnricher enricher = new TradeEnricher(name, processor)

        and:
            InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))

        when:
        '''
            First stage, ensure enricher process the input stream
            and check for the trade data and if it's enriched the data 
            is completed.
        '''
            enricher.processStream (inputStream)

        and:
            Thread.sleep(10L)

        and:
            def tradeData = enricher.getData()

        then:
            tradeData != null
            tradeData.first() == data
            enricher.hasEnriched()

        when:
            '''
                On close, ensure enricher closes/reset the connection
                and enriched flag is reset.
            '''
            enricher.close()

        and:
            tradeData = enricher.getData()

        then:
            tradeData == null
            !enricher.hasEnriched()

        where:
            name                |   data
            'trade-enricher'    |  '20160101,1,EUR,10.0'

    }



}
