package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "연동 작업 목록 조회 요청 파라미터")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SyncJobParameterRequest {

  @Schema(description = "연동 작업 유형", example = "INDEX_INFO")
  private IntegrationJobType jobType;

  @Schema(description = "지수 정보 ID", example = "1")
  private Long indexInfoId;

  @Schema(description = "대상 날짜 시작", example = "2025-06-10")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateFrom;

  @Schema(description = "대상 날짜 종료", example = "2025-06-12")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateTo;

  @Schema(description = "작업자", example = "192.168.0.1")
  private String worker;

  @Schema(description = "작업 일시 시작", example = "2025-06-10T09:00:00")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeFrom;

  @Schema(description = "작업 일시 종료", example = "2025-06-12T18:00:00")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeTo;

  @Schema(description = "작업 상태", example = "SUCCESS")
  private IntegrationResultType status;

  @Schema(description = "이전 페이지 마지막 요소 ID", example = "100")
  private Long idAfter;

  @Schema(description = "커서 (다음 페이지 시작점)", example = "eyJpZCI6MjB9")
  private String cursor; // Case 1(jobTime) 2025-06-09T09:25:16.890996 , Case 2(targetDate) 2023-01-03

  @Schema(description = "정렬 필드", example = "jobTime")
  private String sortField; //  ex) jobTime(작업일시) , targetDate(대상날짜)

  @Schema(description = "정렬 방향", example = "desc")
  private String sortDirection; // asc, desc

  @Schema(description = "페이지 크기", example = "10")
  private Integer size;


}