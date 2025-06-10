package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.entity.IndexData;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

public interface IndexDataService {

  IndexChartResponse getChart(Long indexInfoId, PeriodType periodType)
      throws IOException, URISyntaxException;

  // 지수 데이터
  IndexData create(IndexDataCreateRequest request);

  List<IndexDataDto> findAllByBaseDateBetween(Long indexInfoId, LocalDate from, LocalDate to,
      String sortBy, String sortDirection);

  IndexData update(Long id, IndexDataUpdateRequest request);

  void delete(Long id);
}
