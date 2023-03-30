package com.scb.risk.trade.processor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scb.risk.trade.model.ProductData;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.staticdata.ProductDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BaseTradeProcessor implements TradeProcessor<TradeRawData, TradeData> {
    private static Logger log = LoggerFactory.getLogger(BaseTradeProcessor.class);

    private final BlockingQueue<TradeRawData> processQueue = Queues.newPriorityBlockingQueue();

    private final BlockingQueue<TradeData> enrichedTrades = Queues.newPriorityBlockingQueue();
    private final String name;

    private final ProductDataService productDataService;
    ScheduledExecutorService scheduler;
    public BaseTradeProcessor(String name, int poolSize) {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkArgument(poolSize > 0, "pool size must be positive");
        this.name = name;
        this.productDataService = ProductDataService.getInstance();

        scheduler = Executors.newScheduledThreadPool(poolSize,
                new ThreadFactoryBuilder()
                        .setDaemon(false)
                        .setNameFormat("trade-processor-%d")
                        .build());

        scheduler.scheduleWithFixedDelay(this::process, 1000, 100, TimeUnit.MILLISECONDS);
    }

    public boolean offer(TradeRawData data) {
        return processQueue.offer(data);
    }

    public int count() {
        return enrichedTrades.size();
    }

    public Collection<TradeData> pop() {
        if (enrichedTrades.isEmpty()) {
            log.error("Empty enriched trades.");
            return null;
        }

        Collection<TradeData> trades = Lists.newArrayListWithExpectedSize(enrichedTrades.size());
        int tradeCount = enrichedTrades.drainTo(trades);
        log.info("Returning enriched trades, count=[{}]", tradeCount);
        return trades;
    }

    @VisibleForTesting
    public void process() {

        if (processQueue.isEmpty())
            return;

        log.info("Processing raw trades, trade size = [{}]", processQueue.size());

        List<TradeRawData> rawTrades = Lists.newArrayListWithCapacity(processQueue.size());
        int count = processQueue.drainTo(rawTrades);
        long startTime = System.currentTimeMillis();

        log.info("Processing the enrichment of trades, count=[{}]", count);
        List<TradeData> processedTrades = rawTrades.stream().map(this::enrichTrades).collect(Collectors.toList());

        long totalTimeInMillis = System.currentTimeMillis() - startTime;
        log.info("Completed enriching trades, count=[{}], in [{}] msecs", enrichedTrades, totalTimeInMillis);

        enrichedTrades.addAll(processedTrades);
    }

    @VisibleForTesting
    TradeData enrichTrades(TradeRawData rawData) {
        log.info("Enriching the raw trade data, {}", rawData);
        ProductData staticData = productDataService.getProduct(rawData.id);
        if (staticData == null) {
            log.error("Error while fetching product data for trade, {}", rawData);
        }

        TradeData tradeData = new TradeData(rawData.date,
                staticData == null ? "Missing Product Name" : staticData.name,
                rawData.currency, rawData.price);
        return tradeData;
    }

}
