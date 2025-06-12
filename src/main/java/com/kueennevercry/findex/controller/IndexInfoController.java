package com.kueennevercry.findex.controller;

import com.kueennevercry.findex.dto.request.IndexInfoCreateRequest;
import com.kueennevercry.findex.dto.request.IndexInfoUpdateRequest;
import com.kueennevercry.findex.dto.request.IndexInfoListRequest;
import com.kueennevercry.findex.dto.response.IndexInfoDto;
import com.kueennevercry.findex.dto.response.CursorPageResponse;
import com.kueennevercry.findex.dto.IndexInfoSummaryDto;
import com.kueennevercry.findex.service.IndexInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  /**
   * 지수 정보 목록 조회 (커서 기반 페이징)
   * GET /api/index-infos
   * 
   * 커서 기반 페이징을 사용하여 대용량 데이터도 효율적으로 조회할 수 있습니다.
   * 
   * 지원하는 정렬 옵션:
   * - indexClassification (분류명): asc/desc
   * - indexName (지수명): asc/desc
   * 
   * 사용 예시:
   * 1. 첫 페이지: GET /api/index-infos?size=10
   * 2. 다음 페이지: GET /api/index-infos?cursor=KOSPI지수&idAfter=123&size=10
   * 3. 필터링: GET
   * /api/index-infos?indexClassification=KOSPI지수&favorite=true&size=10
   * 4. 분류명 내림차순: GET
   * /api/index-infos?sortField=indexClassification&sortDirection=desc&size=10
   * 5. 지수명 오름차순: GET
   * /api/index-infos?sortField=indexName&sortDirection=asc&size=10
   */
  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexInfoDto>> getIndexInfoList(
      // === 필터링 파라미터 ===
      @RequestParam(required = false) String indexClassification,
      @RequestParam(required = false) String indexName,
      @RequestParam(required = false) Boolean favorite,

      // === 커서 기반 페이징 파라미터 ===
      // 커서 페이징의 핵심: 이전 페이지의 마지막 항목 정보를 기준으로 다음 페이지 조회
      @RequestParam(required = false) Long idAfter, // 이전 페이지 마지막 항목의 ID (모든 정렬에서 보조 기준으로 사용)
      @RequestParam(required = false) String cursor, // 이전 페이지 마지막 항목의 정렬 기준 값 (분류명 또는 지수명)

      // === 정렬 및 페이징 파라미터 ===
      @RequestParam(defaultValue = "indexClassification") String sortField, // 정렬 기준 필드
      @RequestParam(defaultValue = "asc") String sortDirection, // 정렬 방향 (asc/desc)
      @RequestParam(defaultValue = "10") Integer size) { // 한 페이지당 조회할 데이터 개수

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

    // 서비스 계층에서 커서 기반 페이징 로직 처리
    // 반환되는 CursorPageResponse에는 다음 정보가 포함됨:
    // - content: 실제 데이터 목록
    // - nextCursor: 다음 페이지 요청 시 사용할 커서 값
    // - nextIdAfter: 다음 페이지 요청 시 사용할 ID
    // - hasNext: 다음 페이지 존재 여부
    // - totalElements: 필터 조건에 맞는 전체 데이터 개수
    CursorPageResponse<IndexInfoDto> response = indexInfoService.findWithCursorPaging(request);

    return ResponseEntity.ok(response);
  }

  /**
   * 지수 정보 요약 목록 조회 GET /api/index-infos/summaries
   * 지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.
   */
  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
    List<IndexInfoSummaryDto> summaries = indexInfoService.findAllSummaries();
    return ResponseEntity.ok(summaries);
  }

  /**
   * 특정 ID의 지수 정보 조회 GET /api/index-infos/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> getIndexInfo(@PathVariable Long id) {
    IndexInfoDto indexInfo = indexInfoService.findById(id);
    return ResponseEntity.ok(indexInfo);
  }

  /**
   * 지수 정보 등록 POST /api/index-infos
   */
  @PostMapping
  public ResponseEntity<IndexInfoDto> createIndexInfo(@RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto createdIndexInfo = indexInfoService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdIndexInfo);
  }

  /**
   * 지수 정보 수정 PATCH /api/index-infos/{id}
   */
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
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIndexInfo(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity.noContent().build(); // HTTP 204 No Content
  }
}
