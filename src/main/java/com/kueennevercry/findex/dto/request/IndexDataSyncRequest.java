package com.kueennevercry.findex.dto.request;

import java.time.LocalDate;
import java.util.List;

/* 지수 데이터 연동 요청 */
public record IndexDataSyncRequest(
    List<Long> indexInfoIds,
    LocalDate baseDateFrom,
    LocalDate baseDateTo
) {

}