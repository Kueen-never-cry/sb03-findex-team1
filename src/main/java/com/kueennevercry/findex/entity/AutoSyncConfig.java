package com.kueennevercry.findex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "auto_sync_config")
public class AutoSyncConfig {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false, unique = true)
  private IndexInfo indexInfo;

  @Setter
  @Column(name = "enabled", nullable = false)
  private boolean enabled = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  public AutoSyncConfig(IndexInfo indexInfo) {
    this.indexInfo = indexInfo;
    this.enabled = false;
    this.createdAt = Instant.now();
  }

  @PrePersist // 데이터 생성 시 자동으로 실행
  protected void onCreate() {
    createdAt = Instant.now();
  }

  @PreUpdate // 데이터 수정 시 자동으로 실행
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public Long getIndexInfoId() {
    return indexInfo != null ? indexInfo.getId() : null;
  }
}