package com.kueennevercry.findex.dto.request;

import java.time.LocalDate;
import java.util.List;

/* 지수 데이터 연동 요청 */
public class IndexDataSyncRequest {

  List<Integer> indexInfoIds;
  LocalDate baseDateFrom;
  LocalDate baseDateTo;
}
