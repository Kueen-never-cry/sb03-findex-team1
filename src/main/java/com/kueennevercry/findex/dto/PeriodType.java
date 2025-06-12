package com.kueennevercry.findex.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "차트 또는 성과 조회의 기간 단위")
public enum PeriodType {

  @Schema(description = "일간")
  DAILY,

  @Schema(description = "주간")
  WEEKLY,

  @Schema(description = "월간")
  MONTHLY,

  @Schema(description = "분기별")
  QUARTERLY,

  @Schema(description = "연간")
  YEARLY
}