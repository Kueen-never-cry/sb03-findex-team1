package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 지수 정보 DTO 데이터 전달 목적으로만 사용
 */
@Schema(description = "지수 정보 응답 DTO")
public record IndexInfoDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long id,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "기준 시점 구성 종목 수", example = "200")
    Integer employedItemsCount,

    @Schema(description = "기준 시점 날짜", example = "2000-01-01")
    LocalDate basePointInTime,

    @Schema(description = "기준 지수", example = "1000.00")
    BigDecimal baseIndex,

    @Schema(description = "지수 생성 방식", example = "OPEN_API")
    SourceType sourceType,

    @Schema(description = "즐겨찾기 여부", example = "true")
    Boolean favorite,

    @Schema(description = "생성 시각", example = "2025-06-12T10:00:00Z")
    Instant createdAt,

    @Schema(description = "수정 시각", example = "2025-06-12T10:05:00Z")
    Instant updatedAt) {

}