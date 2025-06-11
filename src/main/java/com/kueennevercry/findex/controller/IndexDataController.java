package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.service.IndexDataService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  //----------- 지수 데이터 --------------//
  @GetMapping("/{indexInfoId}")
  public ResponseEntity<List<IndexDataDto>> findByIndexInfoIdAndBaseDateRange(
      @PathVariable Long indexInfoId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "baseDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection
  ) {
    return ResponseEntity.ok(
        indexDataService.findAllByBaseDateBetween(indexInfoId, from, to, sortBy, sortDirection));
  }

  @PostMapping
  public ResponseEntity<IndexData> create(
      @RequestBody IndexDataCreateRequest dto
  ) {
    IndexData indexData = indexDataService.create(dto);

    return ResponseEntity.ok(indexData);
  }

  @PatchMapping("{id}")
  public ResponseEntity<IndexData> update(
      @PathVariable Long id,
      @RequestBody IndexDataUpdateRequest dto
  ) {
    IndexData indexData = indexDataService.update(id, dto);

    return ResponseEntity.ok(indexData);
  }

  @DeleteMapping("{id}")
  public void delete(
      @PathVariable Long id
  ) {
    indexDataService.delete(id);
  }

  //----------- 대시보드 --------------//
  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartDto> getChart(
      @PathVariable Long id,
      @RequestParam PeriodType periodType
  ) {
    IndexChartDto response = indexDataService.getChart(id, periodType);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRank(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(defaultValue = "DAILY") PeriodType periodType,
      @RequestParam(defaultValue = "10") int limit
  ) {
    return ResponseEntity.ok(
        indexDataService.getPerformanceRanking(indexInfoId, periodType, limit));
  }

  @GetMapping("/performance/favorite")
  public List<IndexPerformanceDto> getFavoriteIndexPerformances(
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    return indexDataService.getFavoritePerformances(periodType);
  }
}

