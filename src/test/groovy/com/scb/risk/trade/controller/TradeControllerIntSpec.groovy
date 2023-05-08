package com.scb.risk.trade.controller

import com.google.common.io.Resources
import org.springframework.mock.web.DelegatingServletInputStream
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TradeControllerIntSpec extends Specification {

    void "when request received, process for the input stream and validate/check the response stream"() {

        given:
            TradeController controller = new TradeController()

        and:
            def resource = Resources.getResource("trades.csv").openStream()
            HttpServletRequest request = Mock() {
                _ * getInputStream() >> new DelegatingServletInputStream(resource)
            }

        and:
            List<String> actualTrades = []
            HttpServletResponse response = Mock() {
                _ * getWriter() >> Mock(PrintWriter) {
                    _ * print(_) >> {
                        args ->
                            String data = args[0]
                            if (data != null && !data.startsWith("date"))
                                actualTrades.addAll(data.tokenize('\n'))
                    }
                }
            }

        when:
            controller.enrich(request, response)

        then:
        '''
            First, validate if the actual trades and expected trades size matches
            Second, remove all the expected trades from actual trade list, result should be zero,
            meaning, all the data should be matched between actual vs expected list.
        '''
            actualTrades != null
            actualTrades.size() == expectedTrades.size()

        and:
            actualTrades.removeAll(expectedTrades)
            actualTrades.size() == 0

        where:
        index   | expectedTrades
        0       | ["20160101,Treasury Bills Domestic,EUR,10.0",
                    "20160101,Corporate Bonds Domestic,EUR,20.1",
                    "20160101,REPO Domestic,EUR,30.34",
                    "20160101,Missing Product Name,EUR,35.34"]
    }

}
