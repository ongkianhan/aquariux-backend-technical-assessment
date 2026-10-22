package com.aquariux.technical.assessment.trade.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Data
public class TradeResponse {

    public TradeResponse(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    // TODO: What should you return after a trade is executed?
    private Long timestamp;
    private Long tradeId;
    private BigDecimal totalAmt;
    private BigDecimal price;
    private BigDecimal qty;
    private BigDecimal pairName;
    private BigDecimal baseAsset;
    private BigDecimal quoteAsset;
    private String errorMsg;
}