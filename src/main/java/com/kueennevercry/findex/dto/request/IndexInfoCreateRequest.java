package com.kueennevercry.findex.dto.request;

import com.kueennevercry.findex.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 지수 정보 생성 요청 DTO 데이터 전달 목적으로만 사용
 */
@Schema(description = "지수 정보 생성 요청")
public record IndexInfoCreateRequest(

    @Schema(description = "지수 분류명", example = "KOSPI시리즈", required = true)
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스", required = true)
    String indexName,

    @Schema(description = "기준 시점 구성 종목 수", example = "200", required = true)
    Integer employedItemsCount,

    @Schema(description = "기준 시점 날짜", example = "2000-01-01", required = true)
    LocalDate basePointInTime,

    @Schema(description = "기준 지수", example = "1000", required = true)
    BigDecimal baseIndex,

    @Schema(description = "지수 생성 방식", example = "OPEN_API", required = true)
    SourceType sourceType,

    @Schema(description = "즐겨찾기 여부", example = "true")
    Boolean favorite) {

}