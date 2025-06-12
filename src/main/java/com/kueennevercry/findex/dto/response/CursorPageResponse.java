package com.kueennevercry.findex.dto.response;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

  /**
   * 전체 개수를 포함한 페이지 응답 생성
   */
  public static <T> CursorPageResponse<T> of(
      List<T> content,
      String nextCursor,
      Long nextIdAfter,
      boolean hasNext,
      long totalElements) {
    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        content.size(),
        totalElements,
        hasNext);
  }
}