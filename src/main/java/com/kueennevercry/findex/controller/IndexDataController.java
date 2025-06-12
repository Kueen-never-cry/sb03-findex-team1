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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {

  private final IndexDataService indexDataService;

  //----------- 지수 데이터 --------------//
  @Operation(summary = "지수 데이터 목록 조회", description = "지수 데이터 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 데이터 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataDto>> findByIndexInfoIdAndBaseDateRange(
      @Parameter(description = "지수 정보 ID") @RequestParam(required = false) Long indexInfoId,
      @Parameter(description = "시작 일자") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "종료 일자") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @Parameter(description = "이전 페이지 마지막 요소 ID") @RequestParam(required = false) Long idAfter,
      @Parameter(description = "커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "정렬 필드") @RequestParam(defaultValue = "baseDate") String sortField,
      @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
  ) {
    if (indexInfoId == null) {
      indexInfoId = 3L;
    }
    if (startDate == null) {
      startDate = LocalDate.of(1900, 1, 1);
    }
    if (endDate == null) {
      endDate = LocalDate.of(2100, 1, 1);
      ;
    }

    return ResponseEntity.ok(
        indexDataService.findAllByBaseDateBetween(indexInfoId, startDate, endDate, idAfter, cursor,
            sortField,
            sortDirection, size));
  }

  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지수 데이터 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "참조하는 지수 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public ResponseEntity<IndexData> create(
      @RequestBody IndexDataCreateRequest dto
  ) {
    IndexData indexData = indexDataService.create(dto);

    return ResponseEntity.ok(indexData);
  }

  @Operation(summary = "지수 데이터 수정", description = "기존 지수 데이터를 수정합니다.")
  @PatchMapping("{id}")
  public ResponseEntity<IndexData> update(
      @Parameter(description = "지수 데이터 ID") @PathVariable Long id,
      @RequestBody IndexDataUpdateRequest dto
  ) {
    IndexData indexData = indexDataService.update(id, dto);

    return ResponseEntity.ok(indexData);
  }

  @Operation(summary = "지수 데이터 삭제", description = "지수 데이터를 삭제합니다.")
  @ApiResponse(responseCode = "204", description = "지수 데이터 삭제 성공")
  @DeleteMapping("{id}")
  public void delete(
      @Parameter(description = "지수 데이터 ID") @PathVariable Long id
  ) {
    indexDataService.delete(id);
  }

  //----------- 대시보드 --------------//
  @Operation(summary = "지수 차트 조회", description = "지수의 차트 데이터를 조회합니다.")
  @GetMapping("/{id}/chart")
  public ResponseEntity<IndexChartDto> getChart(
      @Parameter(description = "지수 정보 ID") @PathVariable Long id,
      @Parameter(description = "차트 기간 유형") @RequestParam PeriodType periodType
  ) {
    IndexChartDto response = indexDataService.getChart(id, periodType);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "지수 성과 랭킹 조회", description = "지수의 성과 분석 랭킹을 조회합니다.")
  @GetMapping("/performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceDto>> getRank(
      @Parameter(description = "지수 정보 ID") @RequestParam(required = false) Long indexInfoId,
      @Parameter(description = "성과 기간 유형") @RequestParam(defaultValue = "DAILY") PeriodType periodType,
      @Parameter(description = "최대 랭킹 수") @RequestParam(defaultValue = "10") int limit
  ) {
    return ResponseEntity.ok(
        indexDataService.getPerformanceRanking(indexInfoId, periodType, limit));
  }

  @Operation(summary = "관심 지수 성과 조회", description = "즐겨찾기로 등록된 지수들의 성과를 조회합니다.")
  @GetMapping("/performance/favorite")
  public List<IndexPerformanceDto> getFavoriteIndexPerformances(
      @Parameter(description = "성과 기간 유형") @RequestParam(defaultValue = "DAILY") PeriodType periodType
  ) {
    return indexDataService.getFavoritePerformances(periodType);
  }

  @Operation(summary = "지수 데이터 CSV export", description = "지수 데이터를 CSV 파일로 export합니다.")
  @GetMapping("/export/csv")
  public void exportIndexDataToCsv(
      @Parameter(description = "지수 정보 ID") @RequestParam(required = false) Long indexInfoId,
      @Parameter(description = "시작 일자") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @Parameter(description = "종료 일자") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @Parameter(description = "정렬 필드") @RequestParam(defaultValue = "baseDate") String sortField,
      @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
      HttpServletResponse response
  ) throws IOException {
    Sort.Direction direction = Sort.Direction.fromString(sortDirection);
    Sort sort = Sort.by(direction, sortField);

    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String filename = "index-data-export-" + today + ".csv";
    String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");

    OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
        StandardCharsets.UTF_8);
    writer.write('\uFEFF');

    List<String[]> csvData = indexDataService.getExportableIndexData(indexInfoId, startDate,
        endDate, sort);

    try (CSVWriter csvWriter = new CSVWriter(writer)) {
      csvWriter.writeAll(csvData);
    }
  }
}

