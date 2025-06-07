package com.kueennevercry.findex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "index_info")
@Data
@NoArgsConstructor
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
  private Float baseIndex;

  @Column(name = "source_type", nullable = false, length = 32, columnDefinition = "VARCHAR(32) CHECK (source_type IN ('USER', 'OPEN_API'))")
  private SourceType sourceType;

  @Column(name = "favorite")
  private Boolean favorite;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist // 데이터 생성 시 자동으로 실행
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate // 데이터 수정 시 자동으로 실행
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public enum SourceType {
    USER, OPEN_API
  }
}
