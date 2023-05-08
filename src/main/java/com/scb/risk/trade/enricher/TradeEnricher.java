package com.scb.risk.trade.enricher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.processor.TradeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TradeEnricher implements Enricher<TradeData>{

    private static Logger log = LoggerFactory.getLogger(TradeEnricher.class);
    private final String name;
    private final TradeProcessor processor;

    private AtomicBoolean hasEnrichedTrades = new AtomicBoolean(false);
    BlockingQueue<TradeData> tradeData = Queues.newLinkedBlockingDeque();
    ScheduledExecutorService scheduler;

    TradeEnricher(String name, TradeProcessor processor) {
        Preconditions.checkNotNull(name, "name is empty/null");
        Preconditions.checkNotNull(processor, "TradeProcessor arg is null");
        this.name = name;
        this.processor = processor;

        scheduler = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder()
                        .setDaemon(false)
                        .setNameFormat(this.name+"-%d")
                        .build());

        scheduler.scheduleWithFixedDelay(this::processEnrichedTrades, 1L, 1L, TimeUnit.MILLISECONDS);

    }

    @Override
    public void processStream(InputStream inputStream) throws RuntimeException, InterruptedException {

        log.info("Received input stream, process/enrich the trades..");
        try {
            long initialLoadCount = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .filter(row -> row != null && (row.startsWith("2") || row.startsWith("1")))
                    .map(row -> {
                        String[] tokenizedData = row.split(",");
                        TradeRawData rawData = new TradeRawData(tokenizedData[0], Long.parseLong(tokenizedData[1]),
                                tokenizedData[2], Double.parseDouble(tokenizedData[3]));
                        return processor.offer(rawData);
                    }).count();

            processor.initialLoadDone();
            log.info("Initial load of trade size=[{}] completed.", initialLoadCount);

        } catch (Exception e) {
            log.error("Error enriching trade data, ", e);
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    void processEnrichedTrades() {
        Collection<TradeData> processedTrades = processor.pop();

        if (processedTrades == null || processedTrades.size() == 0) return;

        tradeData.addAll(processedTrades);
    }

    @Override
    public Collection<? extends TradeData> getData() {

        if (tradeData.size() == 0) {
            return null;
        }

        Collection<TradeData> trades = Lists.newArrayListWithExpectedSize(tradeData.size());
        tradeData.drainTo(trades);
        hasEnrichedTrades.set(processor.isDone());

        return trades;
    }

    @Override
    public boolean hasEnriched() {
        return hasEnrichedTrades.get();
    }

    @Override
    public void close() {
        tradeData.clear();
        hasEnrichedTrades.set(false);
        processor.close();
    }
}
