package com.kueennevercry.findex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "integration_tasks")
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

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

}
