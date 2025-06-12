package com.kueennevercry.findex.dto.request;

/**
 * 지수 정보 목록 조회 요청 DTO
 * 커서 기반 페이징과 다양한 필터링 옵션을 제공합니다.
 */
public record IndexInfoListRequest(

    String indexClassification,
    String indexName,
    Boolean favorite,
    Long idAfter,
    String cursor,
    String sortField,
    String sortDirection,
    Integer size) {

  /**
   * 정렬 필드 검증
   * 
   * 허용되는 정렬 필드:
   * - "indexClassification": 지수 분류명 기준 정렬
   * - "indexName": 지수명 기준 정렬
   */
  public boolean isValidSortField() {
    if (sortField == null)
      return true;
    return sortField.equals("indexClassification") ||
        sortField.equals("indexName");
  }

  /**
   * 정렬 방향 검증
   */
  public boolean isValidSortDirection() {
    if (sortDirection == null)
      return true;
    return sortDirection.equals("asc") || sortDirection.equals("desc");
  }

  /**
   * 페이지 크기 검증
   */
  public boolean isValidSize() {
    if (size == null)
      return true;
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