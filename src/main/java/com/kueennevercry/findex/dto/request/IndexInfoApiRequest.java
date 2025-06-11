package com.kueennevercry.findex.infra.openapi;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class IndexInfoApiRequest {

  private int pageNo;// 페이지번호

  private int numOfRows; // 한 페이지 결과 수

  private String basDt; // 검색값과 기준일자가 일치하는 데이터를 검색 ex)20250610

  private String beginBasDt; // 기준일자가 검색값보다 크거나 같은 데이터를 검색

  private String endBasDt; // 기준일자가 검색값보다 작은 데이터를 검색

}
