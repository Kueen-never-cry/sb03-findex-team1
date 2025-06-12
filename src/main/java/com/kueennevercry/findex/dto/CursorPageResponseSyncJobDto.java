package com.kueennevercry.findex.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/*커서 기반 페이지 응답 */
@Schema(description = "연동 작업 커서 기반 페이지 응답 DTO")
@Getter
@ToString
@RequiredArgsConstructor
public class CursorPageResponseSyncJobDto {

  @ArraySchema(schema = @Schema(implementation = SyncJobDto.class))
  private final List<SyncJobDto> content;

  @Schema(description = "다음 페이지 커서", example = "eyJpZCI6MjB9")
  private final String nextCursor;

  @Schema(description = "다음 페이지 ID 기준값", example = "20")
  private final Long nextIdAfter;

  @Schema(description = "페이지 크기", example = "10")
  private final int size;

  @Schema(description = "전체 항목 수", example = "100")
  private final Long totalElements;

  @Schema(description = "다음 페이지 존재 여부", example = "true")
  private final boolean hasNext;
}
