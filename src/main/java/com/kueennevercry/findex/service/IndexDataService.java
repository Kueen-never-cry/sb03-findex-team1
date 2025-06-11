package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;

public interface IndexDataService {

  // 지수 데이터
  IndexData create(IndexDataCreateRequest request);

  List<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to,
      String sortBy, String sortDirection);

  IndexData update(Long id, IndexDataUpdateRequest request);

  void delete(Long id);

  // 대시보드
  IndexChartDto getChart(Long indexInfoId, PeriodType periodType);

  List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfoId, PeriodType periodType,
      int limit);

  List<IndexPerformanceDto> getFavoritePerformances(PeriodType periodType);
}
