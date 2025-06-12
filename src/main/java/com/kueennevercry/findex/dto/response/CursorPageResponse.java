package com.kueennevercry.findex.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 페이지 응답")
public record CursorPageResponse<T>(

    @ArraySchema(schema = @Schema(description = "응답 데이터 목록"))
    List<T> content,

    @Schema(description = "다음 페이지 조회를 위한 커서", example = "20")
    String nextCursor,

    @Schema(description = "다음 페이지 조회를 위한 기준 ID", example = "20")
    Long nextIdAfter,

    @Schema(description = "현재 페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "100")
    long totalElements,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
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