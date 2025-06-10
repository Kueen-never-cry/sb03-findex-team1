package com.kueennevercry.findex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Table(name = "index_info")
@AllArgsConstructor
@Builder
public class IndexInfo {

  @Id // Primary Key
  @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL(AUTO_INCREMENT)
  private Long id;

  @Column(name = "index_classification", nullable = false, length = 32)
  private String indexClassification;

  @Column(name = "index_name", nullable = false, length = 100)
  private String indexName;

  @Column(name = "employed_items_count", nullable = false)
  private Integer employedItemsCount;

  @Column(name = "base_point_in_time", nullable = false)
  private LocalDate basePointInTime;

  @Column(name = "base_index", nullable = false)
  private BigDecimal baseIndex;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 32)
  private SourceType sourceType;

  @Column(name = "favorite")
  private Boolean favorite;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist // 데이터 생성 시 자동으로 실행
  protected void onCreate() {
    createdAt = Instant.now();
  }

  @PreUpdate // 데이터 수정 시 자동으로 실행
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
