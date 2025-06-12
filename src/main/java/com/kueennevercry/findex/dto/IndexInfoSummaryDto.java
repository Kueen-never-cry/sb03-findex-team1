package com.kueennevercry.findex.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지수 요약 정보 DTO")
public record IndexInfoSummaryDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long id,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification
) {

}
