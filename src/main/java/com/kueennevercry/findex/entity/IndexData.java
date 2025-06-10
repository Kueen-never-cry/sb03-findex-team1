package com.kueennevercry.findex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Table(name = "index_data")
public class IndexData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date")
  private LocalDate baseDate;

  @Column(name = "source_type")
  @Enumerated(EnumType.STRING)
  private SourceType sourceType;

  @Column(name = "market_price")
  private BigDecimal marketPrice;

  @Column(name = "closing_price")
  private BigDecimal closingPrice;

  @Column(name = "high_price")
  private BigDecimal highPrice;

  @Column(name = "low_price")
  private BigDecimal lowPrice;

  @Column(name = "versus")
  private BigDecimal versus;

  @Column(name = "fluctuation_rate")
  private BigDecimal fluctuationRate;

  @Column(name = "trading_quantity")
  private Long tradingQuantity;

  @Column(name = "trading_price")
  private Long tradingPrice;

  @Column(name = "market_total_amount")
  private Long marketTotalAmount;

  @Column(name = "created_at")
  private final Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder
  public IndexData(IndexInfo indexInfo, LocalDate baseDate, SourceType sourceType, BigDecimal marketPrice,
                   BigDecimal closingPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal versus, BigDecimal fluctuationRate,
                   Long tradingQuantity, Long tradingPrice, Long marketTotalAmount) {

    this.indexInfo = indexInfo;
    this.baseDate = baseDate;
    this.sourceType = sourceType;
    this.marketPrice = marketPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.marketTotalAmount = marketTotalAmount;
    this.tradingPrice = tradingPrice;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void update(BigDecimal marketPrice, BigDecimal closingPrice,
                     BigDecimal highPrice, BigDecimal lowPrice, BigDecimal versus, BigDecimal fluctuationRate,
                     Long tradingQuantity) {

    boolean anyValueUpdated = false;

    // 시가
    if (marketPrice != null && !marketPrice.equals(this.marketPrice)) {
      this.marketPrice = marketPrice;
      anyValueUpdated = true;
    }

    // 종가
    if (closingPrice != null && !closingPrice.equals(this.closingPrice)) {
      this.closingPrice = closingPrice;
      anyValueUpdated = true;
    }

    // 고가
    if (highPrice != null && !highPrice.equals(this.highPrice)) {
      this.highPrice = highPrice;
      anyValueUpdated = true;
    }

    // 저가
    if (lowPrice != null && !lowPrice.equals(this.lowPrice)) {
      this.lowPrice = lowPrice;
      anyValueUpdated = true;
    }

    // 대비
    if (versus != null && !versus.equals(this.versus)) {
      this.versus = versus;
      anyValueUpdated = true;
    }

    // 등락률
    if (fluctuationRate != null && !fluctuationRate.equals(this.fluctuationRate)) {
      this.fluctuationRate = fluctuationRate;
      anyValueUpdated = true;
    }

    // 거래량
    if (tradingQuantity != null && !tradingQuantity.equals(this.tradingQuantity)) {
      this.tradingQuantity = tradingQuantity;
      anyValueUpdated = true;
    }

    if (anyValueUpdated) {
      this.sourceType = SourceType.USER;
      this.updatedAt = Instant.now();
    }
  }
}
