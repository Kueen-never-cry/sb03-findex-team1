package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 생성 요청 DTO 데이터 전달 목적으로만 사용
 */
public record IndexInfoCreateRequest(
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    Boolean favorite) {

}