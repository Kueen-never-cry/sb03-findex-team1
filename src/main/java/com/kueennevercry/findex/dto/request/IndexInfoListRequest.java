package com.kueennevercry.findex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 지수 정보 목록 조회 요청 DTO 커서 기반 페이징과 다양한 필터링 옵션을 제공합니다.
 */
@Schema(description = "지수 정보 목록 조회 요청")
public record IndexInfoListRequest(

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "즐겨찾기 여부", example = "true")
    Boolean favorite,

    @Schema(description = "이전 페이지 마지막 요소 ID", example = "20")
    Long idAfter,

    @Schema(description = "커서 (다음 페이지 시작점)", example = "eyJpZCI6MjB9")
    String cursor,

    @Schema(description = "정렬 필드 (indexClassification, indexName, employedItemsCount)", example = "indexClassification")
    String sortField,

    @Schema(description = "정렬 방향 (asc, desc)", example = "asc")
    String sortDirection,

    @Schema(description = "페이지 크기", example = "10")
    Integer size) {

  /**
   * 정렬 필드 검증
   * 
   * 허용되는 정렬 필드:
   * - "indexClassification": 지수 분류명 기준 정렬
   * - "indexName": 지수명 기준 정렬
   */
  public boolean isValidSortField() {
    if (sortField == null) {
      return true;
    }
    return sortField.equals("indexClassification") ||
        sortField.equals("indexName");
  }

  /**
   * 정렬 방향 검증
   */
  public boolean isValidSortDirection() {
    if (sortDirection == null) {
      return true;
    }
    return sortDirection.equals("asc") || sortDirection.equals("desc");
  }

  /**
   * 페이지 크기 검증
   */
  public boolean isValidSize() {
    if (size == null) {
      return true;
    }
    return size > 0 && size <= 100;
  }

  /**
   * 기본값 적용된 요청 객체 반환
   */
  public IndexInfoListRequest withDefaults() {
    return new IndexInfoListRequest(
        this.indexClassification,
        this.indexName,
        this.favorite,
        this.idAfter,
        this.cursor,
        this.sortField != null ? this.sortField : "indexClassification",
        this.sortDirection != null ? this.sortDirection : "asc",
        this.size != null ? this.size : 10);
  }
}