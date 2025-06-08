package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IndexDataService {

  IndexChartResponse getChart(Long indexInfoId, PeriodType periodType)
      throws IOException, URISyntaxException;

  List<RankedIndexPerformanceDto> getPerformanceRanking(Long indexInfoId, String periodType,
      int limit);
}
