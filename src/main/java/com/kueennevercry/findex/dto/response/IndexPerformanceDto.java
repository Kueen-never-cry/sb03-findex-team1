package com.kueennevercry.findex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "지수 성과 응답 DTO")
public record IndexPerformanceDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long indexInfoId,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "전일 대비 등락", example = "50.5")
    BigDecimal versus,

    @Schema(description = "등락률 (%)", example = "1.8")
    BigDecimal fluctuationRate,

    @Schema(description = "현재 지수", example = "2850.75")
    BigDecimal currentPrice,

    @Schema(description = "전일 지수", example = "2800.25")
    BigDecimal beforePrice
) {

}
