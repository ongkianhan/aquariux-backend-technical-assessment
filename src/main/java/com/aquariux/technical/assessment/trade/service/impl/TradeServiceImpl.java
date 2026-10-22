package com.aquariux.technical.assessment.trade.service.impl;

import com.aquariux.technical.assessment.trade.controller.WalletController;
import com.aquariux.technical.assessment.trade.dto.internal.UserWalletDto;
import com.aquariux.technical.assessment.trade.dto.request.TradeRequest;
import com.aquariux.technical.assessment.trade.dto.response.TradeResponse;
import com.aquariux.technical.assessment.trade.dto.response.WalletBalanceResponse;
import com.aquariux.technical.assessment.trade.entity.CryptoPair;
import com.aquariux.technical.assessment.trade.entity.CryptoPrice;
import com.aquariux.technical.assessment.trade.entity.Trade;
import com.aquariux.technical.assessment.trade.entity.UserWallet;
import com.aquariux.technical.assessment.trade.enums.TradeType;
import com.aquariux.technical.assessment.trade.mapper.CryptoPairMapper;
import com.aquariux.technical.assessment.trade.mapper.CryptoPriceMapper;
import com.aquariux.technical.assessment.trade.mapper.TradeMapper;
import com.aquariux.technical.assessment.trade.mapper.UserWalletMapper;
import com.aquariux.technical.assessment.trade.service.TradeServiceInterface;
import com.aquariux.technical.assessment.trade.service.WalletServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeServiceInterface {

    private final TradeMapper tradeMapper;
    // Add additional beans here if needed for your implementation

    private final RestTemplate restTemplate;
    private final UserWalletMapper userWalletMapper;
    private final CryptoPriceMapper cryptoPriceMapper;
    private final CryptoPairMapper cryptoPairMapper;

    private static final String BINANCE_URL = "https://api.binance.com/api/v3/order";
    private static final String HUOBI_URL = "https://api.huobi.pro/v1/order/orders/place";

    @Override
    public TradeResponse executeTrade(TradeRequest tradeRequest) {
        // TODO: Implement the core trading engine
        // What should happen when a user executes a trade?

        var tradeUserId = tradeRequest.getUserId();
        var timestamp = System.currentTimeMillis();
        var tradeTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
        tradeRequest.setTradeTime(tradeTime);
        tradeRequest.setId(timestamp);
        var coin = tradeRequest.getCryptoPairName().substring(0,4);

        // check for current prices
        CryptoPrice latestPrice = tradeMapper.findLatestPriceByPairName(tradeRequest.getCryptoPairName());
        var qty = tradeRequest.getQuantity();
        var price = tradeRequest.getTradeType().equals(TradeType.BUY) ? latestPrice.getAskPrice() : latestPrice.getBidPrice();
        BigDecimal totalAmount = qty.multiply(price);
        var source = tradeRequest.getTradeType().equals(TradeType.BUY) ? latestPrice.getAskSource() : latestPrice.getBidSource();

        // check if wallet balance is available
        List<UserWalletDto> balances = userWalletMapper.findByUserId(tradeUserId);
        UserWalletDto usdtBalObj = balances.stream()
                .filter(bal -> bal.getUserId().equals(tradeUserId) && bal.getSymbol().equals("USDT"))
                .findFirst()
                .orElse(null);

        UserWalletDto coinBalObj = balances.stream()
                .filter(bal -> bal.getUserId().equals(tradeUserId) && bal.getSymbol().equals(coin))
                .findFirst()
                .orElse(null);

        var bal = usdtBalObj == null ? BigDecimal.ZERO : usdtBalObj.getBalance();
        var coinBal = coinBalObj == null ? BigDecimal.ZERO : coinBalObj.getBalance();

        if (bal != null && !bal.equals(BigDecimal.ZERO) && usdtBalObj.getBalance().compareTo(totalAmount) >= 0) {
            // we have enough balance to execute, execute the trade
            try {
                if (source.equals("BINANCE")) {
                    restTemplate.postForObject(BINANCE_URL, tradeRequest, TradeResponse.class);
                } else if (source.equals("HUOBI")) {
                    restTemplate.postForObject(HUOBI_URL, tradeRequest, TradeResponse.class);
                }

                if (coinBalObj == null) {
                    UserWalletDto newBal = new UserWalletDto();
                    newBal.setBalance(BigDecimal.ZERO);
                    newBal.setId(balances.size() + 1L);
                    newBal.setName(coin);
                    newBal.setSymbol(coin);
                    newBal.setUserId(usdtBalObj.getUserId());
                    newBal.setUpdatedAt(tradeTime);
                    coinBalObj = newBal;
                }

                Trade trade = createTrade(tradeRequest);

                if (trade != null) {
                    tradeMapper.insertTrade(trade);
                }

                if (tradeRequest.getTradeType().equals(TradeType.BUY)) {
                    usdtBalObj.setBalance(bal.subtract(totalAmount));
                    coinBalObj.setBalance(coinBal.add(qty));
                } else {
                    usdtBalObj.setBalance(bal.add(totalAmount));
                    coinBalObj.setBalance(coinBal.subtract(qty));
                }

                tradeMapper.updateWalletBalance(usdtBalObj);
                log.info("USDT Balance update for trade id {}: new balance {}", timestamp, usdtBalObj.getBalance());
                tradeMapper.updateWalletBalance(coinBalObj);
                log.info("{} Balance update for trade id {}: new balance {}", coin, timestamp, coinBalObj.getBalance());

            } catch (Exception e) {
                log.error("Unable to execute trade: {}", e.getMessage());
                return new TradeResponse(e.getMessage());
            }

        }

        throw new UnsupportedOperationException("Trade execution logic to be implemented");
    }

    public Trade createTrade(TradeRequest request) {
        Trade trade = new Trade();
        trade.setId(request.getId());
        trade.setUserId(request.getUserId());
        trade.setCryptoPairId(request.getCryptoPairId());
        trade.setTradeType(request.getTradeType().toString());
        trade.setQuantity(request.getQuantity());
        trade.setPrice(request.getPrice());
        trade.setTotalAmount(request.getTotalAmt());
        trade.setTradeTime(request.getTradeTime());

        return trade;
    }
}