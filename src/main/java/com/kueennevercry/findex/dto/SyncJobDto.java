package com.kueennevercry.findex.dto;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*연동 작업 DTO */
@Schema(description = "연동 작업 응답 DTO")
@Getter
@Setter
@ToString
public class SyncJobDto {

  @Schema(description = "연동 작업 ID", example = "1")
  private Long id;

  @Schema(description = "연동 작업 유형", example = "INDEX_DATA")
  private IntegrationJobType jobType;

  @Schema(description = "지수 정보 ID", example = "1")
  private Long indexInfoId;

  @Schema(description = "대상 기준일", example = "2023-01-01")
  private LocalDate targetDate;

  @Schema(description = "작업자 IP", example = "192.168.0.1")
  private String worker;

  @Schema(description = "작업 실행 시각", example = "2023-01-01T12:00:00Z")
  private Instant jobTime;

  @Schema(description = "연동 작업 결과", example = "SUCCESS")
  private IntegrationResultType result;
}
