package com.kueennevercry.findex.dto.request;

import java.time.LocalDate;
import java.util.List;

public class IndexDataSyncRequest {

  List<Long> indexInfoIds;
  LocalDate baseDateFrom;
  LocalDate baseDateTo;
}
