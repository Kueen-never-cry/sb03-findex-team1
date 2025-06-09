package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

/*연동 작업 DTO */
@Getter
public class SyncJobDto {

  private Long id;
  private IntegrationJobType jobType;
  private int indexInfoId;
  private LocalDate targetDate;
  private LocalDateTime jobTime;
  private IntegrationResultType result;
}
