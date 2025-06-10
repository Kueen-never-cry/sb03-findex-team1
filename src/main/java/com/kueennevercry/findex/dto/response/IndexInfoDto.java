package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.entity.SourceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 지수 정보 DTO 데이터 전달 목적으로만 사용
 */
public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    Boolean favorite,
    Instant createdAt,
    Instant updatedAt) {

}