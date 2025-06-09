package com.kueennevercry.findex.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*커서 기반 페이지 응답 */
@Getter
@RequiredArgsConstructor
public class CursorPageResponseSyncJobDto {

  private final List<SyncJobDto> content;
  private final String nextCursor;
  private final Long nextIdAfter;
  private final int size;
  private final int totalElements;
  private final boolean hasNext;
}
