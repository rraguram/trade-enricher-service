package com.scb.risk.trade.controller;

import com.scb.risk.trade.enricher.Enricher;
import com.scb.risk.trade.enricher.EnricherFactory;
import com.scb.risk.trade.model.TradeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/")
@Configuration
@PropertySource("classpath:application.properties")
public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final Enricher<TradeData> tradeEnricher;

    public TradeController() {
        this.tradeEnricher = EnricherFactory.createTradeEnricher();
    }

    @RequestMapping(path = "enrich/", method = RequestMethod.POST)
    public void enrich(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Processing the input trade request.");

        try {
            tradeEnricher.processStream(request.getInputStream());

            String csvOutput = "date,product_name,currency,price\n";
            response.setContentType("text/csv; charset=utf-8");
            response.getWriter().print(csvOutput);

            while(!tradeEnricher.hasEnriched()) {
                Collection<? extends TradeData> trades = tradeEnricher.getData();

                if (trades != null && trades.size() > 0) {
                    String tradeData = trades.stream()
                            .map(TradeData::toString)
                            .collect(Collectors.joining("\n"));

                    response.getWriter().print(tradeData);
                }
            }

            logger.info("Completed enriching the trades.");
        } catch (RuntimeException | InterruptedException ie) {
            logger.error("Error processing the trade stream, {}", ie);
        } finally {
            tradeEnricher.close();
        }

    }
}
