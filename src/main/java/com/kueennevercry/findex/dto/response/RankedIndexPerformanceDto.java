package com.kueennevercry.findex.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지수 성과 랭킹 응답 DTO")
public record RankedIndexPerformanceDto(

    @Schema(description = "랭킹", example = "1")
    Integer rank,

    @Schema(description = "지수 성과 정보")
    IndexPerformanceDto performance
) {

}
