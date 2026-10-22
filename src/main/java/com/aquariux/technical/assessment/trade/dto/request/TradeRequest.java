package com.aquariux.technical.assessment.trade.dto.request;

import com.aquariux.technical.assessment.trade.enums.TradeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeRequest {
    // TODO: What information do you need to execute a trade?
    // referencing trades database provided in schema
    // KIV type (market, limit etc.) and timeInForce from Binance and Huobi API docs

    private Long id;
    private Long userId;
    private TradeType tradeType;
    private String cryptoPairName;
    private BigDecimal quantity;
    @JsonIgnore
    private BigDecimal price;
    @JsonIgnore
    private BigDecimal totalAmt;
    @JsonIgnore
    private Long cryptoPairId;
    private LocalDateTime tradeTime;
}