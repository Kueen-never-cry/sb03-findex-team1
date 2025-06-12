package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.dto.request.IndexInfoUpdateRequest;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.service.IndexInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  /**
   * 지수 정보 목록 조회 (커서 기반 페이징) GET /api/index-infos
   * <p>
   * 커서 기반 페이징을 사용하여 대용량 데이터도 효율적으로 조회할 수 있습니다.
   * <p>
   * 사용 예시: 1. 첫 페이지: GET /api/index-infos?size=10 2. 다음 페이지: GET
   * /api/index-infos?idAfter=123&size=10 3. 필터링: GET
   * /api/index-infos?indexClassification=KOSPI지수&favorite=true&size=10 4. 정렬: GET
   * /api/index-infos?sortField=indexName&sortDirection=desc&size=10
   */
  @Operation(summary = "지수 정보 목록 조회", description = "지수 정보 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexInfoDto>> getIndexInfoList(
      @Parameter(description = "지수 분류명")
      @RequestParam(required = false) String indexClassification,
      @Parameter(description = "지수명")
      @RequestParam(required = false) String indexName,
      @Parameter(description = "즐겨찾기 여부")
      @RequestParam(required = false) Boolean favorite,
      @Parameter(description = "이전 페이지 마지막 요소 ID")
      @RequestParam(required = false) Long idAfter,
      @Parameter(description = "커서 (다음 페이지 시작점)")
      @RequestParam(required = false) String cursor,
      @Parameter(description = "정렬 필드 (indexClassification, indexName, employedItemsCount)")
      @RequestParam(defaultValue = "indexClassification") String sortField,
      @Parameter(description = "정렬 방향 (asc, desc)")
      @RequestParam(defaultValue = "asc") String sortDirection,
      @Parameter(description = "페이지 크기")
      @RequestParam(defaultValue = "10") Integer size) {

    // 요청 파라미터를 DTO로 변환
    IndexInfoListRequest request = new IndexInfoListRequest(
        indexClassification,
        indexName,
        favorite,
        idAfter,
        cursor,
        sortField,
        sortDirection,
        size);

    // 서비스 호출
    CursorPageResponse<IndexInfoDto> response = indexInfoService.findWithCursorPaging(request);

    return ResponseEntity.ok(response);
  }

  /**
   * 지수 정보 요약 목록 조회 GET /api/index-infos/summaries 지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.
   */
  @Operation(summary = "지수 정보 요약 목록 조회", description = "지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "지수 정보 요약 목록 조회 성공")
  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
    List<IndexInfoSummaryDto> summaries = indexInfoService.findAllSummaries();
    return ResponseEntity.ok(summaries);
  }

  /**
   * 특정 ID의 지수 정보 조회 GET /api/index-infos/{id}
   */
  @Operation(summary = "지수 정보 조회", description = "ID로 지수 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "지수 정보 조회 성공")
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfo(@PathVariable Long id) {
    IndexInfoDto indexInfo = indexInfoService.findById(id);
    return ResponseEntity.ok(indexInfo);
  }

  /**
   * 지수 정보 등록 POST /api/index-infos
   */
  @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 등록합니다.")
  @ApiResponse(responseCode = "201", description = "지수 정보 생성 성공")
  @PostMapping
  public ResponseEntity<IndexInfoDto> createIndexInfo(@RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto createdIndexInfo = indexInfoService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdIndexInfo);
  }

  /**
   * 지수 정보 수정 PATCH /api/index-infos/{id}
   */
  @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
  @ApiResponse(responseCode = "200", description = "지수 정보 수정 성공")
  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> updateIndexInfo(
      @PathVariable Long id,
      @RequestBody IndexInfoUpdateRequest request) {

    // 요청 데이터가 모두 null인 경우 체크
    if (request.employedItemsCount() == null &&
        request.basePointInTime() == null &&
        request.baseIndex() == null &&
        request.favorite() == null) {
      throw new IllegalArgumentException("수정할 데이터가 없습니다.");
    }

    IndexInfoDto updatedIndexInfo = indexInfoService.update(id, request);
    return ResponseEntity.ok(updatedIndexInfo);
  }

  /**
   * 지수 정보 삭제 DELETE /api/index-infos/{id}
   */
  @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
  @ApiResponse(responseCode = "204", description = "지수 정보 삭제 성공")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity.noContent().build(); // HTTP 204 No Content
  }
}
