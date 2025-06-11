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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "integration_tasks")
@AllArgsConstructor
@Builder
public class IntegrationTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false)
  private IntegrationJobType jobType;

  @Column(name = "target_date", nullable = true)
  private LocalDate targetDate;

  @Column(name = "worker", nullable = false)
  private String worker;

  @Column(name = "job_time", nullable = false)
  private Instant jobTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false)
  private IntegrationResultType result;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @PrePersist // 데이터 생성 시 자동으로 실행
  protected void onCreate() {
    createdAt = Instant.now();
  }

}
