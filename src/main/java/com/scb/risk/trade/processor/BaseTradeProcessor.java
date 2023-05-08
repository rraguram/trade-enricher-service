package com.scb.risk.trade.processor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.scb.risk.trade.model.ProductData;
import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;
import com.scb.risk.trade.staticdata.ReferenceData;
import com.scb.risk.trade.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <p>BaseTradeProcessor receives the raw trade data from the streaming input
 * channel and transforms the underlying trade data by looking up the
 * reference data and further validates on the trade.</p>
 *
 * @See ReferenceData
 * @See Validator
 */
public class BaseTradeProcessor implements TradeProcessor<TradeRawData, TradeData> {
    private static Logger log = LoggerFactory.getLogger(BaseTradeProcessor.class);

    private final BlockingQueue<TradeRawData> processQueue = Queues.newLinkedBlockingDeque();

    private final BlockingQueue<TradeData> enrichedTrades = Queues.newLinkedBlockingDeque();

    private final AtomicLong tradeCount = new AtomicLong(0L);
    private final AtomicLong enrichedTradeCount = new AtomicLong(0L);
    private final AtomicLong invalidTradeCount = new AtomicLong(0L);

    private final AtomicBoolean isInitialLoadDone = new AtomicBoolean(false);
    private final String name;
    private final ReferenceData<ProductData> productDataService;

    private final Validator<TradeData> validator;
    ScheduledExecutorService scheduler;
    public BaseTradeProcessor(String name, ReferenceData productDataService, Validator validator) {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(productDataService, "ReferenceData is null");
        Preconditions.checkNotNull(validator, "Validator is null");

        this.name = name;
        this.productDataService = productDataService;
        this.validator = validator;

        int cpuPoolSize = Runtime.getRuntime().availableProcessors();
        scheduler = Executors.newScheduledThreadPool(cpuPoolSize,
                new ThreadFactoryBuilder()
                        .setDaemon(false)
                        .setNameFormat(this.name+"-%d")
                        .build());

        scheduler.scheduleWithFixedDelay(this::process, 1L, 1L, TimeUnit.MILLISECONDS);
    }

    public boolean offer(TradeRawData data) {
        tradeCount.addAndGet(1L);
        return processQueue.offer(data);
    }

    public int count() {
        return enrichedTrades.size();
    }

    public void initialLoadDone() {
        this.isInitialLoadDone.getAndSet(true);
    }

    public boolean isDone() {
        return isInitialLoadDone.get() &&
                (tradeCount.get() - (enrichedTradeCount.get() + invalidTradeCount.get()) == 0) ? true : false;
    }

    public Collection<TradeData> pop() {
        if (enrichedTrades.isEmpty()) {
            return null;
        }

        Collection<TradeData> trades = Lists.newArrayListWithExpectedSize(enrichedTrades.size());
        long processedTradeCount = enrichedTrades.drainTo(trades);
        long totalProcessedCount = enrichedTradeCount.addAndGet(processedTradeCount);
        log.info("Returning enriched trades, count=[{}], total raw trade count total=[{}], enriched trades=[{}], invalid trades count=[{}]", processedTradeCount, tradeCount, totalProcessedCount, invalidTradeCount);

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
        List<TradeData> processedTrades = rawTrades.stream()
                .map(this::enrichTrades)
                .filter(this::isValid).
                collect(Collectors.toList());

        long totalTimeInMillis = System.currentTimeMillis() - startTime;
        log.info("Completed enriching trades, count=[{}], in [{}] msecs", processedTrades.size(), totalTimeInMillis);

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

    /**
     * Perform validation on the underlying trade
     * @param tradeData
     * @return valid, if trade check is performed
     */
    @VisibleForTesting
    boolean isValid(TradeData tradeData) {
        boolean isValid = validator.isValid(tradeData);
        if (!isValid) invalidTradeCount.addAndGet(1L);

        return isValid;
    }

    public void close() {
        this.tradeCount.getAndSet(0L);
        this.enrichedTradeCount.getAndSet(0L);
        this.invalidTradeCount.getAndSet(0L);
        this.isInitialLoadDone.getAndSet(false);
        this.enrichedTrades.clear();
        this.processQueue.clear();
    }
}
