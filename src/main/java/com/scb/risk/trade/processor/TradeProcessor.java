package com.scb.risk.trade.processor;

import com.scb.risk.trade.model.TradeData;
import com.scb.risk.trade.model.TradeRawData;

import java.util.Collection;

/**
 * Abstract implementation of trade processor for enriching and validation
 * of the underlying trades.
 * @param <T> Returns the processed trades
 * @param <R> Input of the raw trades
 */
public interface TradeProcessor<R extends TradeRawData, T extends TradeData> {

    public boolean offer(R data);

    public int count();

    public boolean isDone();

    public void initialLoadDone();

    public Collection<T> pop();

    public void process();

    public void close();
}
