package com.scb.risk.trade.model;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

public final class TradeData implements Comparable<TradeData> {

    public final String date;

    public final String name;

    public final String currency;

    public final double price;


    public TradeData(String date, String name, String currency, double price) {
        this.date = date;
        this.name = name;
        this.currency = currency;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeData tradeData = (TradeData) o;
        return Objects.equal(date, tradeData.date) && Objects.equal(name, tradeData.name) && Objects.equal(currency, tradeData.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(date, name, currency);
    }

    @Override
    public String toString() {
        return new StringBuffer()
                .append(date).append(",")
                .append(name).append(",")
                .append(currency).append(",")
                .append(price).toString();
    }

    @Override
    public int compareTo(TradeData o) {
        return ComparisonChain.start()
                .compare(date, o.date)
                .compare(name, o.name)
                .compare(currency, o.currency)
                .result();
    }
}
