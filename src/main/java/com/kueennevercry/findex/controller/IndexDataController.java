package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.service.IndexDataService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartResponse> getChart(
      @PathVariable Long id,
      @RequestParam PeriodType periodType
  ) throws IOException, URISyntaxException {
    IndexChartResponse response = indexDataService.getChart(id, periodType);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRank(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(defaultValue = "DAILY") String periodType,
      @RequestParam(defaultValue = "10") int limit
  ) {
    return ResponseEntity.ok(
        indexDataService.getPerformanceRanking(indexInfoId, periodType, limit));
  }
}

