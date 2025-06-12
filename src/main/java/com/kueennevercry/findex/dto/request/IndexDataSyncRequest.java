package com.kueennevercry.findex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/* 지수 데이터 연동 요청 */
@Schema(description = "지수 데이터 동기화 요청")
public record IndexDataSyncRequest(

    @Schema(description = "동기화할 지수 정보 ID 목록", example = "[1, 2, 3]", required = true)
    List<Long> indexInfoIds,

    @Schema(description = "시작 날짜", example = "2023-01-01", required = true)
    LocalDate baseDateFrom,
    
    @Schema(description = "종료 날짜", example = "2023-01-31", required = true)
    LocalDate baseDateTo
) {

}