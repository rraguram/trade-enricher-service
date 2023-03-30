package com.scb.risk.trade.model;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public final class TradeRawData implements Comparable<TradeRawData> {

    public final String date;
    public final long id;
    public final String currency;
    public final double price;


    public TradeRawData(String date, long id, String currency, double price) {
        this.date = date;
        this.id = id;
        this.currency = currency;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeRawData rawData = (TradeRawData) o;
        return id == rawData.id && Objects.equal(date, rawData.date) && Objects.equal(currency, rawData.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(date, id, currency);
    }

    @Override
    public int compareTo(TradeRawData o) {
        return ComparisonChain.start()
                .compare(date, o.date)
                .compare(id, o.id)
                .compare(currency, o.currency)
                .result();
    }

    @Override
    public String toString() {
        return "TradeRawData{" +
                "date='" + date + '\'' +
                ", id=" + id +
                ", currency='" + currency + '\'' +
                ", price=" + price +
                '}';
    }
}
