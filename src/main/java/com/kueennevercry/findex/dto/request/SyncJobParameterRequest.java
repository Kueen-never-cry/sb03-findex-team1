package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.entity.IntegrationJobType;
import com.kueennevercry.findex.entity.IntegrationResultType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
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
  private Integer cursor;
  private String sortField; // TODO : 이거 어떤 타입 있는지 알아봐야함 ex) jobTime
  private String sortDirection; // desc
  private Integer size;

  @Override
  public String toString() {
    return "SyncJobParameterRequest{" +
        "jobType='" + jobType + '\'' +
        ", indexInfoId=" + indexInfoId +
        ", baseDateFrom='" + baseDateFrom + '\'' +
        ", baseDateTo='" + baseDateTo + '\'' +
        ", worker='" + worker + '\'' +
        ", jobTimeFrom='" + jobTimeFrom + '\'' +
        ", jobTimeTo='" + jobTimeTo + '\'' +
        ", status='" + status + '\'' +
        ", idAfter=" + idAfter +
        ", cursor=" + cursor +
        ", sortField='" + sortField + '\'' +
        ", sortDirection='" + sortDirection + '\'' +
        ", size=" + size +
        '}';
  }
}