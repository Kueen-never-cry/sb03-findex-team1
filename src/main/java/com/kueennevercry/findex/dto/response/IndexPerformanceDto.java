package com.kueennevercry.findex.dto.response;

import java.math.BigDecimal;

public record IndexPerformanceDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    float versus,
    float fluctuationRate,
    BigDecimal currentPrice,
    BigDecimal beforePrice
) {

}
