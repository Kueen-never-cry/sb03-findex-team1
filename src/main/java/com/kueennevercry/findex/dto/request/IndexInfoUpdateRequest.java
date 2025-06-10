package com.kueennevercry.findex.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 수정 요청 DTO 수정 가능한 필드: 채용 종목 수, 기준 시점, 기준 지수, 즐겨찾기
 */
public record IndexInfoUpdateRequest(
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    Boolean favorite) {

}
