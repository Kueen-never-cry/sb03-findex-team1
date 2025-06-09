package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.IndexDataDto;
import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.IndexChartResponse;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.service.IndexDataService;
import com.kueennevercry.findex.service.IndexDataServiceImpl;
import java.io.IOException;
import java.net.URISyntaxException;
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
  private final IndexDataServiceImpl indexDataServiceImpl;

  //----------- 지수 데이터 --------------//
  @GetMapping("/{indexInfoId}")
  public ResponseEntity<List<IndexDataDto>> findByIndexInfoIdAndBaseDateRange(
      @PathVariable(required = false) Long indexInfoId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "baseDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDirection
  ) {
    if (indexInfoId == null) {
      indexInfoId = 3L;
    }
    if (from == null) {
      from = LocalDate.of(1900, 1, 1);
    }
    if (to == null) {
      to = LocalDate.now();
    }
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
  @GetMapping("/favorite")
  public List<IndexPerformanceDto> getFavoriteIndexPerformances(
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    return indexDataServiceImpl.getFavoritePerformances(periodType);
  }

  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartResponse> getChart(
      @PathVariable Long id,
      @RequestParam PeriodType periodType
  ) throws IOException, URISyntaxException {
    IndexChartResponse response = indexDataService.getChart(id, periodType);
    return ResponseEntity.ok(response);
  }

}

