package com.kueennevercry.findex.service;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import java.io.IOException;
import java.net.URISyntaxException;

public interface IndexDataService {

  IndexChartResponse getChart(Long indexInfoId, PeriodType periodType)
      throws IOException, URISyntaxException;
}
