package com.scb.risk.trade.enricher;

import com.google.common.base.Preconditions;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.processor.BaseTradeProcessor;
import com.scb.risk.trade.processor.TradeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

public class TradeEnricher {

    private static Logger log = LoggerFactory.getLogger(TradeEnricher.class);
    private final String name;

    private final long tradeElaspedTimeinMillis;

    private final TradeProcessor processor;

    TradeEnricher(String name, long tradeElaspedTimeinMillis, TradeProcessor processor) {
        Preconditions.checkNotNull(name, "name is empty/null");
        Preconditions.checkNotNull(processor, "TradeProcessor arg is null");
        Preconditions.checkArgument(tradeElaspedTimeinMillis > 0, "trade elapsed time should be positive");
        this.name = name;
        this.processor = processor;
        this.tradeElaspedTimeinMillis = tradeElaspedTimeinMillis;

    }

    public Collection<? extends TradeData> processStream(InputStream inputStream) throws RuntimeException, InterruptedException {

        log.info("Received input stream, process/enrich the trades..");
        long enrichStartTime = System.currentTimeMillis();
        long tradeCount = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .filter(row -> row != null && (row.startsWith("2") || row.startsWith("1")))
                .peek(row -> {
                    log.info("Processing the trade, {}", row);
                })
                .map(row -> {
                    log.info("Enriching the trades, {}", row);
                    String[] tokenizedData = row.split(",");
                    TradeRawData rawData = new TradeRawData(tokenizedData[0], Long.parseLong(tokenizedData[1]),
                            tokenizedData[2], Double.parseDouble(tokenizedData[3]));
                    return processor.offer(rawData);
                }).count();

        int enrichedTradeCount = processor.count();
        long startTime = System.currentTimeMillis();

        while (tradeCount > enrichedTradeCount) {
            log.info("Raw trade count=[{}], processed trades count=[{}]", tradeCount, enrichedTradeCount);

            if ((System.currentTimeMillis() - startTime) > tradeElaspedTimeinMillis) {
                throw new RuntimeException("Time out while processing the enrichment of trades");
            }

            Thread.sleep(1000);

            enrichedTradeCount = processor.count();
        }

        Collection<? extends TradeData> tradeData = processor.pop();
        long totalTime = System.currentTimeMillis() - enrichStartTime;
        log.info("Enriched trades completed, count=[{}], time taken=[{}] msecs", tradeData.size(), totalTime);

        return tradeData;
    }



}
