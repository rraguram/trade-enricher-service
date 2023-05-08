package com.scb.risk.trade.enricher;

import java.io.InputStream;
import java.util.Collection;

/**
 * Enricher provides interface to process the underlying input stream of data
 * and  provides endpoints to retrieve the data and to check if the
 * underlying data has been enriched.
 *
 * @See TradeEnricher
 * @param <T>
 */
public interface Enricher<T> {

    public void processStream(InputStream stream) throws InterruptedException;

    public Collection<? extends T> getData();
    public boolean hasEnriched();

    public void close();

}
