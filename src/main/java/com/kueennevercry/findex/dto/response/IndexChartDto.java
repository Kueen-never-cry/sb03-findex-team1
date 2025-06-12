package com.kueennevercry.findex.dto.response;

import com.kueennevercry.findex.dto.ChartDataPoint;
import com.kueennevercry.findex.dto.PeriodType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지수 차트 응답 DTO")
public record IndexChartDto(

    @Schema(description = "지수 정보 ID", example = "1")
    Long indexInfoId,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "차트 기간 유형", example = "DAILY")
    PeriodType periodType,

    @ArraySchema(schema = @Schema(implementation = ChartDataPoint.class))
    List<ChartDataPoint> dataPoints,

    @ArraySchema(schema = @Schema(implementation = ChartDataPoint.class))
    List<ChartDataPoint> ma5DataPoints,

    @ArraySchema(schema = @Schema(implementation = ChartDataPoint.class))
    List<ChartDataPoint> ma20DataPoints
) {

}
