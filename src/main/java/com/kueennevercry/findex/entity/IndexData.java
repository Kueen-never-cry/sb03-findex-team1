package com.kueennevercry.findex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "index_data",
    uniqueConstraints = @UniqueConstraint(columnNames = {"index_info_id", "base_date"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexData {

  @Id // Primary Key
  @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL(AUTO_INCREMENT)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 20)
  private SourceType sourceType;

  @Column(name = "market_price", nullable = false)
  private Float marketPrice;

  @Column(name = "closing_price", nullable = false)
  private Float closingPrice;

  @Column(name = "high_price", nullable = false)
  private Float highPrice;

  @Column(name = "low_price", nullable = false)
  private Float lowPrice;

  @Column(name = "versus", nullable = false)
  private Float versus;

  @Column(name = "fluctuation_rate", nullable = false)
  private Float fluctuationRate;

  @Column(name = "trading_quantity", nullable = false)
  private Long tradingQuantity;

  @Column(name = "trading_price", nullable = false)
  private Long tradingPrice;

  @Column(name = "market_total_amount", nullable = false)
  private Long marketTotalAmount;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
