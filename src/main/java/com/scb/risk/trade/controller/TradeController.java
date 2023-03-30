package com.scb.risk.trade.controller;

import com.scb.risk.trade.enricher.EnricherFactory;
import com.scb.risk.trade.enricher.TradeEnricher;
import com.scb.risk.trade.model.TradeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
public class TradeController {
    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    @Value("${trades.enrichment.service.pool.size:1}")
    private int tradeProcessorPoolSize=1;

    private final TradeEnricher tradeEnricher;

    public TradeController() {
        this.tradeEnricher = EnricherFactory.create("TradeEnricher", tradeProcessorPoolSize);
    }

    @RequestMapping(path = "enrich/", method = RequestMethod.POST)
    public void enrich(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Processing the input trade request.");

        Collection<? extends TradeData> trades;

        try {
            trades = tradeEnricher.processStream(request.getInputStream());
        } catch (RuntimeException | InterruptedException ie) {
            log.error("Error processing the trade stream, {}", ie);
            return;
        }

        if (trades == null || trades.isEmpty()) {
            log.warn("Empty enriched processed trades from the input stream.");
            return;
        }

        log.info("Processed trades, trades population size, = [{}]", trades.size());
        String tradeData = trades.stream().map(TradeData::toString).collect(Collectors.joining("\n"));
        String csvOutput = "date,product_name,currency,price\n" + tradeData;

        response.setContentType("text/csv; charset=utf-8");
        response.getWriter().print(csvOutput);
    }
}
