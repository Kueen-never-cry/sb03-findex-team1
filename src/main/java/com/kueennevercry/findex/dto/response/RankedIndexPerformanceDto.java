package com.kueennevercry.findex.dto.response;

public record RankedIndexPerformanceDto(
    Integer rank,
    IndexPerformanceDto performance
) {

}
