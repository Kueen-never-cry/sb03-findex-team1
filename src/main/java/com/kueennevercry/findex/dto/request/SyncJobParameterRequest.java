package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SyncJobParameterRequest {

  private IntegrationJobType jobType;
  private Long indexInfoId;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateFrom;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate baseDateTo;
  private String worker;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeFrom;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime jobTimeTo;
  private IntegrationResultType status;
  private Long idAfter;
  private String cursor; // Case 1(jobTime) 2025-06-09T09:25:16.890996 , Case 2(targetDate) 2023-01-03
  private String sortField; //  ex) jobTime(작업일시) , targetDate(대상날짜)
  private String sortDirection; // asc, desc
  private Integer size;


}