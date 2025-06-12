package com.kueennevercry.findex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "지수 API 요청 DTO")
@Builder
@Getter
@Setter
public class IndexInfoApiRequest {

  @Schema(description = "페이지 번호", example = "1")
  private int pageNo;// 페이지번호

  @Schema(description = "한 페이지 결과 수", example = "100")
  private int numOfRows; // 한 페이지 결과 수

  @Schema(description = "지수명 (검색값과 일치하는 지수명)", example = "IT 서비스")
  private String idxNm; // 검색값과 지수명이 일치하는 데이터를 검색

  @Schema(description = "기준일자 (yyyyMMdd)", example = "20250610")
  private String basDt; // 검색값과 기준일자가 일치하는 데이터를 검색 ex)20250610

  @Schema(description = "기준일자 시작 (보다 크거나 같음)", example = "20240101")
  private String beginBasDt; // 기준일자가 검색값보다 크거나 같은 데이터를 검색
  
  @Schema(description = "기준일자 끝 (보다 작음)", example = "20241231")
  private String endBasDt; // 기준일자가 검색값보다 작은 데이터를 검색

}
