package com.scb.risk.trade.validation;

import com.scb.risk.trade.model.TradeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public enum TradeValidator implements Validator<TradeData> {

    DATE_VALID {
        final Logger logger = LoggerFactory.getLogger(TradeValidator.class);
        final String DATE_FORMAT = "yyyyMMdd";
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        public boolean isValid(TradeData data) {
            if (data.date != null) {
                try {
                    formatter.parse(data.date);
                    return true;
                } catch (DateTimeParseException dte) {
                    logger.error("Error parsing trade date with [{}] format, {}, for trade data, {}", DATE_FORMAT, data.date, data);
                }
            }

            return false;
        }
    }

}