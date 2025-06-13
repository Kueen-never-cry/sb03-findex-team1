package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*연동 작업 DTO */
@Getter
@Setter
@ToString
public class SyncJobDto {

  private Long id;
  private IntegrationJobType jobType;
  private Long indexInfoId;
  private LocalDate targetDate;
  private String worker;
  private Instant jobTime;
  private IntegrationResultType result;
}
