package com.aquariux.technical.assessment.trade.mapper;

import com.aquariux.technical.assessment.trade.dto.internal.UserWalletDto;
import com.aquariux.technical.assessment.trade.entity.CryptoPrice;
import com.aquariux.technical.assessment.trade.entity.Trade;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TradeMapper {
    
    // TODO: What database operations do you need for trading?
    // Feel free to add multiple methods, complex queries, or additional mapper interfaces as needed
    @Select("""
        SELECT cp.id, cp.crypto_pair_id as cryptoPairId, cp.bid_price as bidPrice, 
               cp.ask_price as askPrice, cp.bid_source as bidSource, cp.ask_source as askSource, 
               cp.created_at as createdAt, cpair.pair_name as pairName
        FROM crypto_prices cp
        INNER JOIN crypto_pairs cpair ON cp.crypto_pair_id = cpair.id
        WHERE cpair.pair_name = #{pairName}
        ORDER BY cp.created_at DESC
        LIMIT 1
        """)
    CryptoPrice findLatestPriceByPairName(String pairName);


    @Insert("""
            INSERT INTO trades (id, user_id, crypto_pair_id, trade_type, quantity, price, total_amount, trade_time) 
            VALUES (#{id}, #{userId}, #{cryptoPairId}, #{tradeType}, #{quantity}, #{price}, #{totalAmt}, #{tradeTime})
            """)
    void insertTrade(Trade trade);

    @Update("""
            UPDATE SET 
                user_id = #{userId}, 
                crypto_pair_id = #{cryptoPairId},
                trade_type = #{tradeType}, 
                quantity = #{quantity}, 
                price = #{price}, 
                total_amount = #{totalAmt}, 
                trade_time = #{tradeTime} 
            WHERE id = #{id}
            """)
    void updateWalletBalance(UserWalletDto balance);
}