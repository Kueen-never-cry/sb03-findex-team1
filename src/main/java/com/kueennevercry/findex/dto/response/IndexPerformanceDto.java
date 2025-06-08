package com.kueennevercry.findex.dto.response;

public record IndexPerformanceDto(
    Long indexInfoId,
    String indexClassification,
    String indexName,
    float versus,
    float fluctuationRate,
    float currentPrice,
    float beforePrice
) {

}
