package com.kueennevercry.findex.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 수정 요청 DTO 수정 가능한 필드: 채용 종목 수, 기준 시점, 기준 지수, 즐겨찾기
 */
@Schema(description = "지수 정보 수정 요청")
public record IndexInfoUpdateRequest(

    @Schema(description = "채용 종목 수", example = "200")
    Integer employedItemsCount,

    @Schema(description = "기준 시점", example = "2000-01-01")
    LocalDate basePointInTime,

    @Schema(description = "기준 지수", example = "1000.00")
    BigDecimal baseIndex,

    @Schema(description = "즐겨찾기 여부", example = "true")
    Boolean favorite) {

}
