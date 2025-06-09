package com.kueennevercry.findex.dto.request;

import java.time.Instant;
import java.util.List;

/* 지수 데이터 연동 요청 */
public class IndexDataSyncRequest {

  List<Integer> indexInFolds;
  Instant baseDateForm;
  Instant baseDateTo;
}
