package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface IndexDataService {

  // 지수 데이터
  IndexData create(IndexDataCreateRequest request);

  CursorPageResponse<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from,
      LocalDate to,
      Long idAfter, String cursor,
      String sortField, String sortDirection, int size);

  IndexData update(Long id, IndexDataUpdateRequest request);

  void delete(Long id);

  // 대시보드
  IndexChartDto getChart(Long indexInfoId, PeriodType periodType);

  List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfoId, PeriodType periodType,
      int limit);

  List<IndexPerformanceDto> getFavoritePerformances(PeriodType periodType);

  List<String[]> getExportableIndexData(Long indexInfoId, LocalDate startDate,
      LocalDate endDate, Pageable pageable);

}
