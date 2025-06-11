package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.PeriodType;
import com.kueennevercry.findex.dto.request.IndexDataCreateRequest;
import com.kueennevercry.findex.dto.request.IndexDataUpdateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexChartDto;
import com.kueennevercry.findex.dto.response.IndexDataDto;
import com.kueennevercry.findex.dto.response.IndexPerformanceDto;
import com.kueennevercry.findex.dto.response.RankedIndexPerformanceDto;
import com.kueennevercry.findex.entity.IndexData;
import com.kueennevercry.findex.service.IndexDataService;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataDto>> findByIndexInfoIdAndBaseDateRange(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "baseDate") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (indexInfoId == null) {
      indexInfoId = 3L;
    }
    if (startDate == null) {
      startDate = LocalDate.of(1900, 1, 1);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    return ResponseEntity.ok(
        indexDataService.findAllByBaseDateBetween(indexInfoId, startDate, endDate, idAfter, cursor,
            sortField,
            sortDirection, size));
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
  ) throws IOException, URISyntaxException {
    IndexChartDto response = indexDataService.getChart(id, periodType);
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

  @GetMapping("/performance/favorite")
  public List<IndexPerformanceDto> getFavoriteIndexPerformances(
      @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    return indexDataService.getFavoritePerformances(periodType);
  }

  @GetMapping("/export/csv")
  public void exportIndexDataToCsv(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "baseDate") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection,
      HttpServletResponse response
  ) throws IOException {
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    Sort sort = Sort.by(direction, sortField);
    Pageable pageable = PageRequest.of(0, 1, sort);

    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String filename = "index-data-export-" + today + ".csv";
    String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

    OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
        StandardCharsets.UTF_8);
    writer.write('\uFEFF');

    List<String[]> csvData = indexDataService.getExportableIndexData(indexInfoId, startDate,
        endDate, pageable);

    try (CSVWriter csvWriter = new CSVWriter(writer)) {
      csvWriter.writeAll(csvData);
    }
  }
}

